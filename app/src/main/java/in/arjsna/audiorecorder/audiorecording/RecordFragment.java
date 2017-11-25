package in.arjsna.audiorecorder.audiorecording;

import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.jakewharton.rxbinding2.view.RxView;

import java.io.BufferedInputStream;
import java.io.File;

import in.arjsna.audiorecorder.AppConstants;
import in.arjsna.audiorecorder.R;
import in.arjsna.audiorecorder.activities.PlayListActivity;
import in.arjsna.audiorecorder.activities.SettingsActivity;
import in.arjsna.audiorecorder.api.ApiClient;
import in.arjsna.audiorecorder.api.ApiInterface;
import in.arjsna.audiorecorder.audiovisualization.GLAudioVisualizationView;
import in.arjsna.audiorecorder.di.components.ActivityComponent;
import in.arjsna.audiorecorder.di.qualifiers.ActivityContext;
import in.arjsna.audiorecorder.mvpbase.BaseFragment;
import in.arjsna.audiorecorder.recordingservice.AudioRecordService;
import in.arjsna.audiorecorder.recordingservice.AudioRecorder;
import in.arjsna.audiorecorder.recordingservice.Constants;
import in.arjsna.audiorecorder.theme.ThemeHelper;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import javax.inject.Inject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.util.Base64;

import org.apache.commons.io.FileUtils;

public class RecordFragment extends BaseFragment implements AudioRecordMVPView {
  private static final String LOG_TAG = RecordFragment.class.getSimpleName();
  private FloatingActionButton mRecordButton = null;
  private FloatingActionButton mPauseButton = null;
  private GLAudioVisualizationView audioVisualization;

  private TextView chronometer;
  private boolean mIsServiceBound = false;
  private AudioRecordService mAudioRecordService;
  private ObjectAnimator alphaAnimator;
  private FloatingActionButton mSettingsButton;
  private FloatingActionButton mPlayListBtn;

  @Inject
  @ActivityContext
  public AppCompatActivity mContext;

  @Inject
  public AudioRecordPresenter<AudioRecordMVPView> audioRecordPresenter;

  public static RecordFragment newInstance() {
    return new RecordFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityComponent activityComponent = getActivityComponent();
    if (activityComponent != null) {
      activityComponent.inject(this);
      audioRecordPresenter.onAttach(this);
    }
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View recordView = inflater.inflate(R.layout.fragment_record, container, false);
    initViews(recordView);
    bindEvents();
    return recordView;
  }

  private void bindEvents() {
    RxView.clicks(mRecordButton)
        .subscribe(o -> audioRecordPresenter.onToggleRecodingStatus());
    RxView.clicks(mSettingsButton)
        .subscribe(o -> startActivity(new Intent(mContext, SettingsActivity.class)));
    RxView.clicks(mPlayListBtn)
        .subscribe(o -> startActivity(new Intent(mContext, PlayListActivity.class)));
    RxView.clicks(mPauseButton)
        .subscribe(o -> audioRecordPresenter.onTogglePauseStatus());
  }

  private void initViews(View recordView) {
    chronometer = recordView.findViewById(R.id.chronometer);

    audioVisualization = recordView.findViewById(R.id.visualizer_view);

    mSettingsButton = recordView.findViewById(R.id.settings_btn);
    mPlayListBtn = recordView.findViewById(R.id.play_list_btn);
    mRecordButton = recordView.findViewById(R.id.btnRecord);
    mPauseButton = recordView.findViewById(R.id.btnPause);
    mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts

    alphaAnimator =
        ObjectAnimator.ofObject(chronometer, "alpha", new FloatEvaluator(), 0.2f);
    alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
    alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
    audioRecordPresenter.onViewInitialised();
  }

  private void setAsPauseBtn() {
    alphaAnimator.cancel();
    chronometer.setAlpha(1.0f);
    mPauseButton.setImageResource(R.drawable.ic_media_pause);
  }

  private void setAsResumeBtn() {
    alphaAnimator.start();
    mPauseButton.setImageResource(R.drawable.ic_media_record);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    audioVisualization.release();
    audioRecordPresenter.onDetach();
  }

  @Override public void refreshTheme(ThemeHelper themeHelper) {
    GLAudioVisualizationView.ColorsBuilder colorsBuilder =
        new GLAudioVisualizationView.Builder(mContext);
    colorsBuilder.setBackgroundColor(themeHelper.getPrimaryColor());
    colorsBuilder.setLayerColors(themeHelper.getLayerColor());
    audioVisualization.updateConfig(colorsBuilder);
    chronometer.setTextColor(themeHelper.getLayerColor()[3]);
    mRecordButton.setRippleColor(themeHelper.getLayerColor()[3]);
    mSettingsButton.setRippleColor(themeHelper.getLayerColor()[3]);
    mPlayListBtn.setRippleColor(themeHelper.getLayerColor()[3]);
    mPauseButton.setRippleColor(themeHelper.getLayerColor()[3]);
  }

  @Override public void updateChronometer(String text) {
    chronometer.setText(text);
  }

  @Override public void togglePauseStatus() {
    if (audioRecordPresenter.isPaused()) {
      setAsResumeBtn();
    } else {
      setAsPauseBtn();
    }
  }

  @Override
  public void pauseRecord() {
    mAudioRecordService.pauseRecord();
    togglePauseStatus();
  }

  @Override
  public void resumeRecord() {
    mAudioRecordService.resumeRecord();
    togglePauseStatus();
  }

  @Override public void toggleRecordButton() {
    mRecordButton.setImageResource(
        audioRecordPresenter.isRecording() ? R.drawable.ic_media_stop : R.drawable.ic_media_record);
  }

  @Override public void linkGLViewToHandler() {
    audioVisualization.linkTo(mAudioRecordService.getHandler());
  }

  @Override public void setPauseButtonVisible() {
    mPauseButton.setVisibility(View.VISIBLE);
  }

  @Override public void setPauseButtonInVisible() {
    mPauseButton.setVisibility(View.GONE);
  }

  @Override public void setScreenOnFlag() {
    mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  @Override public void clearScreenOnFlag() {
    mContext.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  @Override public void startServiceAndBind() {
    Intent intent = new Intent(mContext, AudioRecordService.class);
    mContext.startService(intent);
    bindToService();
  }

  @Override
  public void bindToService() {
    Intent intent = new Intent(mContext, AudioRecordService.class);
    mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    registerLocalBroadCastReceiver();
  }

  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      mIsServiceBound = true;
      mAudioRecordService =
          ((AudioRecordService.ServiceBinder) iBinder).getService();
      Log.i("Tesing", " " + mAudioRecordService.isRecording() + " recording");
      audioRecordPresenter.onServiceStatusAvailable(mAudioRecordService.isRecording(),
          mAudioRecordService.isPaused());
    }

    @Override public void onServiceDisconnected(ComponentName componentName) {
    }
  };

  @Override
  public void unbindFromService() {
    unRegisterLocalBroadCastReceiver();
    if (mIsServiceBound) {
      mIsServiceBound = false;
      mContext.unbindService(serviceConnection);
    }
  }

  @Override
  public Disposable subscribeForTimer(Consumer<AudioRecorder.RecordTime> recordTimeConsumer) {
    return mAudioRecordService.subscribeForTimer(recordTimeConsumer);
  }

  private void unRegisterLocalBroadCastReceiver() {
    LocalBroadcastManager.getInstance(mContext).unregisterReceiver(serviceUpdateReceiver);
  }

  private void registerLocalBroadCastReceiver() {
    LocalBroadcastManager.getInstance(mContext)
        .registerReceiver(serviceUpdateReceiver, new IntentFilter(AppConstants.ACTION_IN_SERVICE));
  }

  @Override public void stopServiceAndUnBind() {
    Intent intent = new Intent(mContext, AudioRecordService.class);
    mContext.stopService(intent);
    unbindFromService();

    String storeLocation = Environment.getExternalStorageDirectory().getAbsolutePath();
    String mFilePath = storeLocation + "/SoundRecorder/AudioRecord" + Constants.AUDIO_RECORDER_FILE_EXT_WAV;
    File mFile = new File(mFilePath);

    Log.i("Testing", mFile.toString());

    byte[] bytes = new byte [(int) mFile.length()];
    try {
      bytes = FileUtils.readFileToByteArray(mFile);
    } catch (IOException e){

    }

    String encode = Base64.encodeToString(bytes, 0);
    Log.i("Testing", encode);

    //test();
  }

  private boolean test(){
    Retrofit client = ApiClient.getClient();
    ApiInterface service = client.create(ApiInterface.class);

    Call<String> call = service.test();
    call.enqueue(new Callback<String>() {
      @Override
      public void onResponse(Call<String> call, Response<String> response){
        String strResponse = response.body();
        Log.i("Testing", strResponse);
      }

      @Override
      public void onFailure(Call<String> call, Throwable t){
        call.cancel();
      }
    });

    return false;
  }

  private final BroadcastReceiver serviceUpdateReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      if (!intent.hasExtra(AppConstants.ACTION_IN_SERVICE)) return;
      String actionExtra = intent.getStringExtra(AppConstants.ACTION_IN_SERVICE);
      audioRecordPresenter.onServiceUpdateReceived(actionExtra);
    }
  };
}