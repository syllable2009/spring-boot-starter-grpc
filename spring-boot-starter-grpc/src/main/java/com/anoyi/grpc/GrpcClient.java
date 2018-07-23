package com.anoyi.grpc;

import com.anoyi.grpc.config.GrpcProperties;
import io.grpc.*;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GrpcClient {

    private static final Logger log = Logger.getLogger(GrpcClient.class.getName());

    private static final Map<String, ServerContext> serverMap = new HashMap<>();

    public GrpcClient(GrpcProperties grpcProperties) {
        List<RemoteServer> remoteServers = grpcProperties.getRemoteServers();
        if (!CollectionUtils.isEmpty(remoteServers)) {
            for (RemoteServer server : remoteServers) {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(server.getHost(), server.getPort()).usePlaintext().build();
                Class clazz = grpcProperties.getClientInterceptor();
                if (clazz == null) {
                    serverMap.put(server.getServer(), new ServerContext(channel));
                }else {
                    try {
                        ClientInterceptor interceptor = (ClientInterceptor) clazz.newInstance();
                        Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
                        serverMap.put(server.getServer(), new ServerContext(newChannel));
                    } catch (InstantiationException | IllegalAccessException e) {
                        log.warning("ClientInterceptor cannot use, ignoring...");
                        serverMap.put(server.getServer(), new ServerContext(channel));
                    }
                }
            }
        }
    }

    /**
     * 连接远程服务
     */
    public static ServerContext connect(String serverName) {
        return serverMap.get(serverName);
    }

}
