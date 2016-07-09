package com.kingston.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.kingston.base.ServerManager;
import com.kingston.net.Packet;
import com.kingston.net.PacketManager;
import com.kingston.net.PacketType;
import com.kingston.service.login.ClientHeartBeat;
import com.kingston.service.login.LoginManager;
import com.kingston.service.login.ServerLogin;

public class ChatServerHandler extends ChannelHandlerAdapter{
	
	//�ͻ��˳�ʱ����
	private Map<ChannelHandlerContext,Integer> clientOvertimeMap = new ConcurrentHashMap<>();
	private final int MAX_OVERTIME  = 3;  //��ʱ����������ֵ��ע������
	
	@Override
	public void channelRead(ChannelHandlerContext context,Object msg)
			throws Exception{
		Packet  packet = (Packet)msg;
		System.err.println("receive pact,type = " + packet.getClass().getSimpleName());
		if(packet.getPacketType() == PacketType.ServerLogin ){
			ServerLogin loginPact = (ServerLogin)packet;
			LoginManager.INSTANCE.validateLogin(context,loginPact.getUserId(), loginPact.getUserPwd());
			return ;
		}else{
			if(validateSession(packet)){
				PacketManager.INSTANCE.execPacket(packet);
			}
		}
		
		clientOvertimeMap.remove(context);//ֻҪ���ܵ����ݰ�������ճ�ʱ����
		
	}
	
	private  boolean validateSession(Packet loginPact){
		return true;
	}

	@Override
	public void close(ChannelHandlerContext ctx,ChannelPromise promise){
		System.err.println("TCP closed...");
		ctx.close(promise);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.err.println("�ͻ��˹ر�1");
	}
	
	    @Override
	    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
	        ctx.disconnect(promise);
	        System.err.println("�ͻ��˹ر�2");
	    }
	    
	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		    System.err.println("ҵ���߼�����");
		    cause.printStackTrace();
		    //	        ctx.fireExceptionCaught(cause);
		    Channel channel = ctx.channel();
		    if(cause instanceof  IOException && channel.isActive()){
			    System.err.println("simpleclient"+channel.remoteAddress()+"�쳣");
			    ctx.close();
		    }
	    }
	    
	    @Override
	    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			    throws Exception {
		    //������������ʱ
		    if (evt instanceof IdleStateEvent) {
			    IdleStateEvent e = (IdleStateEvent) evt;
			    if (e.state() == IdleState.READER_IDLE) {
				    System.err.println("�ͻ��˶���ʱ");
				    int overtimeTimes = clientOvertimeMap.getOrDefault(ctx, 0);
				    if(overtimeTimes < MAX_OVERTIME){
					    ServerManager.INSTANCE.sendPacketTo(new ClientHeartBeat(), ctx);
					    addUserOvertime(ctx);
				    }else{
					    ServerManager.INSTANCE.ungisterUserContext(ctx);
				    }
			    } 
		    }
	    }
	    
	    private void addUserOvertime(ChannelHandlerContext ctx){
		    int oldTimes = 0;
		    if(clientOvertimeMap.containsKey(ctx)){
			    oldTimes = clientOvertimeMap.get(ctx);
		    }
		    clientOvertimeMap.put(ctx, (int)(oldTimes+1));
	    }
}
