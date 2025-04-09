package org.zookeeperserver.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/zookeeper")
public class ZookeeperStatusController {

    @Value("${zookeeper.client-port}")
    private int clientPort;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        boolean isRunning = checkZookeeperConnection();

        response.put("service", "zookeeper-server");
        response.put("port", clientPort);
        response.put("status", isRunning ? "running" : "not running");

        return ResponseEntity.ok(response);
    }
    private boolean checkZookeeperConnection() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", clientPort), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
