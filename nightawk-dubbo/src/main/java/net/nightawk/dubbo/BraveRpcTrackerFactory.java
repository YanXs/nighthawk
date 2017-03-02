package net.nightawk.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.tracker.*;
import net.nightawk.dubbo.protocol.BraveDubboRpcTracker;
import net.nightawk.dubbo.protocol.http.BraveHttpRpcTracker;

/**
 * @author Xs.
 */
public class BraveRpcTrackerFactory implements RpcTrackerFactory {

    @Override
    public RpcTracker createRpcTracker(URL url) {
        RpcTrackerEngine rpcTrackerEngine = RpcTrackerManager.getRpcTrackerEngine();
        if (rpcTrackerEngine == null) {
            return null;
        }
        RpcTracker rpcTracker = null;
        RpcProtocol rpcProtocol = RpcProtocol.valueOf(url.getProtocol());
        if (rpcProtocol.equals(RpcProtocol.DUBBO)) {
            rpcTracker = new BraveDubboRpcTracker((BraveRpcTrackerEngine) rpcTrackerEngine);
        } else if (rpcProtocol.equals(RpcProtocol.HTTP) || rpcProtocol.equals(RpcProtocol.HESSIAN)) {
            rpcTracker = new BraveHttpRpcTracker(rpcTrackerEngine);
        }
        return rpcTracker;
    }
}
