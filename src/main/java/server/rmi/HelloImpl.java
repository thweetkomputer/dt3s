package server.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HelloImpl extends UnicastRemoteObject implements Hello {
    
    public HelloImpl() throws RemoteException {
        super();
    }
    
    public String sayHello() throws RemoteException {
        return "Hello, world!";
    }
}

