package github.javaguide.transport.socket;

import github.javaguide.dto.RpcRequest;
import github.javaguide.dto.RpcResponse;
import github.javaguide.exception.RpcException;
import github.javaguide.registry.ServiceRegistry;
import github.javaguide.registry.ZkServiceRegistry;
import github.javaguide.transport.ClientTransport;
import github.javaguide.utils.checker.RpcMessageChecker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 基于 Socket 传输 RpcRequest
 *
 * @author shuang.kou
 * @createTime 2020年05月10日 18:40:00
 */
@AllArgsConstructor
public class SocketRpcClient implements ClientTransport {
    private static final Logger logger = LoggerFactory.getLogger(SocketRpcClient.class);
    private final ServiceRegistry serviceRegistry;

    public SocketRpcClient() {
        this.serviceRegistry = new ZkServiceRegistry();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // 通过输出流发送数据到服务端
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            //从输入流中读取出 RpcResponse
            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
            //校验 RpcResponse 和 RpcRequest
            RpcMessageChecker.check(rpcResponse, rpcRequest);
            return rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception when send sendRpcRequest");
            throw new RpcException("调用服务失败:", e);
        }
    }
}
