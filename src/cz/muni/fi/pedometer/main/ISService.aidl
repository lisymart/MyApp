package cz.muni.fi.pedometer.main;

interface ISService {
  void setCallback( in IBinder binder );
  void removeCallback();
  void stopSampling();
  boolean isSampling();
  int getState();
}
