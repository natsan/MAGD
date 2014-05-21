/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author michael
 */
public interface OnServerEventListener {
    
    public void onClientConnected(String ipAddress);

    public void onClientDisconnected(String ipAddress);
    
    public void onRequestRecieved(String ip, byte[] data);
    
}
