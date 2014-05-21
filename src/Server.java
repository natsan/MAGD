/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author michael
 */
public class Server {
    
    private ServerSocket _serverSocket;
    private int _port;
    private int _max_connections = 100;
    private static long _timeout = 30*1000; //ms
    private static long _read_timeout = 500; //ms
    
    private Map _connections = Collections.synchronizedMap(new HashMap());
    private ServerThread _serverThread = new ServerThread();
    private OnServerEventListener _onServerEventListener = new OnServerEventListener() {

        @Override
        public void onClientConnected(String ip) {
            
        }

        @Override
        public void onRequestRecieved(String ip, byte[] data) {
            
        }

        @Override
        public void onClientDisconnected(String ipAddress) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    
    public Server(int port) throws IOException
    {
        _port = port;
        
        _serverSocket = new ServerSocket(_port);
        
    }
    
    public synchronized void setOnServerEventListener(OnServerEventListener onServerEventListener)
    {
        _onServerEventListener = onServerEventListener;
    }
    
    public synchronized void setMaximumConnections(int max_connections)
    {
        _max_connections = max_connections;
    }
    
    public void start()
    {
        _serverThread.start();
    }
    
    public synchronized void stop()
    {
        _serverThread._stop = true;
    }
    
    public synchronized void send(String ip, byte[] data)
    {
        ((Connection)_connections.get(ip))._data_to_write = new byte[data.length];
        System.arraycopy(data, 0, ((Connection)_connections.get(ip))._data_to_write, 0, data.length);
    }
    
    public synchronized void setTimeOut(long timeout)
    {
        _timeout = timeout;
    }
    
    private class ServerThread extends Thread{
        
        public boolean _stop = false;
        
        @Override
        public void run()
        {            
            while(!_stop)
            {
                try {
                    
                    Socket client = _serverSocket.accept();
                    
                    if( Server.this._connections.size() == Server.this._max_connections )
                    {
                        System.out.println("Server: too many connections. Trying to find disconnected...");
                        
                        List<Connection> connectionsToDel = new ArrayList<Connection>();
                        for(Object aConnection : Server.this._connections.values())
                        {
                            if( ((Connection)aConnection)._client.isClosed() )
                            {
                                connectionsToDel.add((Connection)aConnection);
                            }
                        }
                        
                        if( connectionsToDel.size() > 0 )
                        {
                            this.cleanPool(connectionsToDel);
                        }else{
                            System.out.println("Server: Can not clean pool, sorry.");
                            client.close();
                            continue;
                        }
                    }
                    
                    if( Server.this._connections.containsKey(client.getInetAddress().getHostAddress()) &&
			!((Connection)Server.this._connections.get(client.getInetAddress().getHostAddress()))._client.isClosed() )
                    {
                        System.out.println("Server: a connection with " + client.getInetAddress().getHostAddress() + " already exists.");
                        continue;
                    }
                    
                    _onServerEventListener.onClientConnected(client.getInetAddress().getHostAddress());
                    
                    Connection connection = new Connection(client);
                    
                    Server.this._connections.put(client.getInetAddress().getHostAddress(), connection);
                    
                    connection.start();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
            
            closeConnections();
        }
        
        private synchronized void closeConnections()
        {
            for(Object aConnection : Server.this._connections.values())
                ((Connection)aConnection)._stop = true;
        }       
        
        private synchronized void cleanPool(List<Connection> connectionsToDel)
        {            
            for( Connection aConnection : connectionsToDel )
            {
                try {
                    aConnection._client.close();
                    _onServerEventListener.onClientDisconnected(aConnection._client.getInetAddress().getHostAddress());
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }finally{
                    Server.this._connections.remove(aConnection._client.getInetAddress().getHostAddress());
                }                
            }
        }
    }
    
    private class Connection extends Thread{
        
        public boolean _stop = false;
        public byte[] _data_to_write = new byte[0];

        private Socket _client;
        private String _ip;
        private long _last_ping = new Date().getTime();
        private final DataInputStream _in;
        private final DataOutputStream _out;
        private long _last_read = new Date().getTime();

        public Connection(Socket client) throws IOException
        {
            _client = client;
            _ip = client.getInetAddress().getHostAddress();
            _in = new DataInputStream (_client.getInputStream());
            _out = new DataOutputStream(_client.getOutputStream());
        }

        @Override
        public void run()
        {            
            while( !_stop )
            { 
                _onServerEventListener.onRequestRecieved(_ip, read());
                   
                if( _data_to_write.length > 0 )
                {
                    write(_data_to_write);
                    _data_to_write = new byte[0];
                }
            }
        }
        
        private void write(byte[] data)
        {
            System.out.println("Server: Writing; Bytes to write: " + data.length);
            try {
                _out.writeByte(data.length);
                _out.flush();
                _out.write(data);
                _out.flush();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Server: Written " + _out.size() + " bytes");
        }
        
        private byte[] read()
        {
            byte[] buffer = new byte[1];
            String data = new String();

            while( !data.contains("~!~!") )
            {
                if( new Date().getTime() - _last_ping > Server._timeout )
                {
                    System.out.println("Connection " + _client.getInetAddress().getHostAddress() + ": connection timeout.");
                    try {
                        _in.close();
                        _out.flush();
                        _out.close();
                        _client.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    _stop = true;
                    return new byte[0];
                }
                
                try {
                    if( _in.read(buffer, 0, 1) > 0) {
                        _last_read = new Date().getTime();
                        data += new String(buffer);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                if( new Date().getTime() - _last_read > Server._read_timeout ) {
                    //System.out.println("Connection read timeout");
                    return new byte[0];
                }
            }
            
            try {
                _in.read(buffer, 0, 1);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            _last_ping = new Date().getTime();
            //System.out.println(data.length());

            byte[] rawData = new byte[data.length()-4];
            System.arraycopy(data.getBytes(), 0, rawData, 0, rawData.length);
            
            return rawData;
        }
    }
}
