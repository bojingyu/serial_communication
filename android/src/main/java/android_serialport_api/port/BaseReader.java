/*
 * BaseReader.java
 * Created by: Mahad Asghar on 18/08/2022.
 *
 *  Copyright © 2022 BjsSoftSolution. All rights reserved.
 */

package android_serialport_api.port;

public abstract class BaseReader {

    private LogInterceptorSerialPort logInterceptor;
    public String port;
    public boolean isAscii;
    public boolean useCRC8;

    void onBaseRead(String port, boolean isAscii, byte[] buffer, int size, boolean useCRC8) {
        this.port = port;
        this.isAscii = isAscii;
        this.useCRC8 = useCRC8;
        String read;
        if (isAscii) {
            read = new String(buffer, 0, size);
        } else {
            read = TransformUtils.bytes2HexString(buffer, size);
        }
        log(SerialApiManager.read, port, isAscii, new StringBuffer().append(read));
        onParse(port, isAscii, read, useCRC8);
    }

    protected abstract void onParse(String port, boolean isAscii, String read, boolean useCRC8);

    public void setLogInterceptor(LogInterceptorSerialPort logInterceptor) {
        this.logInterceptor = logInterceptor;
    }

    protected void log(@SerialApiManager.Type String type, String port, boolean isAscii, CharSequence log) {
        log(type, port, isAscii, log == null ? "null" : log.toString());
    }

    protected void log(@SerialApiManager.Type String type, String port, boolean isAscii, String log) {
        if (logInterceptor != null) {
            logInterceptor.log(type, port, isAscii, log);
        }
    }

    public static boolean checkCrc(String data) {
        int crc = Integer.parseInt(data.substring(data.length() - 2, data.length()), 16);
        int result = BaseReader.crc(data.substring(0, data.length() - 2));
        if (crc != result) {
            System.err.println(crc + "!=" + result);
            return false;
        }
        return true;
    }

    public static int crc(String data) {
        int[] src = new int[data.length() / 2];
        for (int i = 0; i < src.length; i++) {
            src[i] = Integer.parseInt(data.substring(i * 2, i * 2 + 2), 16);
        }
        int result = 0;
        for (int i = 0; i < src.length; i++) {
            result = (result ^ src[i]) & 0xff;
            for (int j = 8; j > 0; j--) {
                if ((result & 0x80) != 0) {
                    result = (((result & 0xff) << 1) ^ 0x31) & 0xff;
                } else {
                    result = ((result & 0xff) << 1) & 0xff;
                }
            }
        }
        return result & 0xff;
    }
}
