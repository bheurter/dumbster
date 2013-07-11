/*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dumbster.smtp;

import com.dumbster.util.Config;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * Dummy SMTP server for testing purposes.
 */
public class SmtpServer implements Runnable {

    private volatile MailStore mailStore = new NullMailStore();
    private volatile boolean stopped = true;
    private volatile boolean ready = false;
    private volatile boolean threaded = false;

    private ServerSocket serverSocket;
    private int port;

    SmtpServer(int port) {
        this.port = port;
    }

    public boolean isReady() {
        return ready;
    }

    public void run() {
        stopped = false;
        try {
            initializeServerSocket();
            serverLoop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ready = false;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initializeServerSocket() throws Exception {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(Config.getConfig().getServerSocketTimeout());
    }

    private void serverLoop() throws IOException {
        int poolSize = threaded ? Config.getConfig().getMaxThreads() : 1;
        ExecutorService threadExecutor = Executors.newFixedThreadPool(poolSize);
        while (!isStopped()) {
			Socket socket = clientSocket();
			if(null == socket) {
				continue;
			}
			SocketWrapper source = new SocketWrapper(socket);
            ClientSession session = new ClientSession(source, mailStore);
            threadExecutor.execute(session);
        }
        ready = false;
    }

    private Socket clientSocket() throws IOException {
        Socket socket = null;
        while (socket == null && !isStopped()) {
            socket = accept();
        }
        return socket;
    }

    private Socket accept() {
        try {
            ready = true;
            return serverSocket.accept();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public synchronized void stop() {
        stopped = true;
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    public MailMessage[] getMessages() {
        return mailStore.getMessages();
    }

    public MailMessage getMessage(int i) {
        return mailStore.getMessage(i);
    }

    public int getEmailCount() {
        return mailStore.getEmailCount();
    }

    public void anticipateMessageCountFor(int messageCount, int ticks) {
        int tickdown = ticks;
        while (mailStore.getEmailCount() < messageCount && tickdown > 0) {
            tickdown--;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * Toggles if the SMTP server is single or multi-threaded for response to
     * SMTP sessions.
     *
     * @param threaded whether or not to allow multiple simultaneous connections
     */
    public void setThreaded(boolean threaded) {
        this.threaded = threaded;
    }

    public void setMailStore(MailStore mailStore) {
        this.mailStore = mailStore;
    }

    public void clearMessages() {
        this.mailStore.clearMessages();
    }
}
