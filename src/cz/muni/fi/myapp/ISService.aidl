package cz.muni.fi.myapp;

interface ISService {
  void setCallback( in IBinder binder );
  void removeCallback();
  void stopSampling();
  boolean isSampling();
  int getState();
}
