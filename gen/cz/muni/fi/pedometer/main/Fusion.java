/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\Martin\\workspace\\Pedometer\\src\\cz\\muni\\fi\\pedometer\\main\\Fusion.aidl
 */
package cz.muni.fi.pedometer.main;
public interface Fusion extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cz.muni.fi.pedometer.main.Fusion
{
private static final java.lang.String DESCRIPTOR = "cz.muni.fi.pedometer.main.Fusion";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cz.muni.fi.pedometer.main.Fusion interface,
 * generating a proxy if needed.
 */
public static cz.muni.fi.pedometer.main.Fusion asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cz.muni.fi.pedometer.main.Fusion))) {
return ((cz.muni.fi.pedometer.main.Fusion)iin);
}
return new cz.muni.fi.pedometer.main.Fusion.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_sampleCounter:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.sampleCounter(_arg0);
return true;
}
case TRANSACTION_statusMessage:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.statusMessage(_arg0);
return true;
}
case TRANSACTION_draw:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
double[] _arg2;
_arg2 = data.createDoubleArray();
this.draw(_arg0, _arg1, _arg2);
return true;
}
case TRANSACTION_displayStepDetected:
{
data.enforceInterface(DESCRIPTOR);
this.displayStepDetected();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cz.muni.fi.pedometer.main.Fusion
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void sampleCounter(int count) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(count);
mRemote.transact(Stub.TRANSACTION_sampleCounter, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void statusMessage(int state) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(state);
mRemote.transact(Stub.TRANSACTION_statusMessage, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void draw(int type, int sensorType, double[] values) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(type);
_data.writeInt(sensorType);
_data.writeDoubleArray(values);
mRemote.transact(Stub.TRANSACTION_draw, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void displayStepDetected() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_displayStepDetected, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_sampleCounter = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_statusMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_draw = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_displayStepDetected = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void sampleCounter(int count) throws android.os.RemoteException;
public void statusMessage(int state) throws android.os.RemoteException;
public void draw(int type, int sensorType, double[] values) throws android.os.RemoteException;
public void displayStepDetected() throws android.os.RemoteException;
}
