package com.kingston.base;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.kingston.net.Packet;
import com.kingston.util.StringUtil;

public enum ServerManager {
	
	INSTANCE;

	//�������е�¼�û���Ӧ��ͨ�������Ļ�������Ҫ����ҵ�����ݴ���
	private  Map<Integer,ChannelHandlerContext> USER_CHANNEL_MAP  = new ConcurrentHashMap<>();
	//����ͨ�������Ļ�����Ӧ�ĵ�¼�û�����Ҫ���ڷ���
	private Map<ChannelHandlerContext,Integer> CHANNEL_USER_MAP  = new ConcurrentHashMap<>();
	
	public void sendPacketTo(Packet pact,Integer userId){
		if(pact == null || userId <= 0) return;
		
		Map<Integer,ChannelHandlerContext> contextMap  = USER_CHANNEL_MAP;
		if(StringUtil.isEmpty(contextMap)) return;
		
		ChannelHandlerContext targetContext = contextMap.get(userId);
		if(targetContext == null) return;
		
		targetContext.writeAndFlush(pact);
	}
	
	/**
	 *  �����������û��������ݰ�
	 */
	public void sendPacketToAllUsers(Packet pact){
		if(pact == null ) return;
		Map<Integer,ChannelHandlerContext> contextMap  = USER_CHANNEL_MAP;
		if(StringUtil.isEmpty(contextMap)) return;
		
		contextMap.values().forEach( (ctx) -> ctx.writeAndFlush(pact));
		
	}
	
	/**
	 *  ��һ�����û��������ݰ�
	 */
	public void sendPacketTo(Packet pact,ChannelHandlerContext targetContext ){
		if(pact == null || targetContext == null) return;
		targetContext.writeAndFlush(pact);
	}
	
	public ChannelHandlerContext getOnlineContextBy(String userId){
		return USER_CHANNEL_MAP.get(userId);
	}
	
	public Map<Integer,ChannelHandlerContext> getAllOnlineContext(){
		return USER_CHANNEL_MAP;
	}
	
	public void addOnlineContext(Integer userId,ChannelHandlerContext context){
		if(context == null){
			throw new NullPointerException("context is null");
		}
		USER_CHANNEL_MAP.put(userId,context);
		CHANNEL_USER_MAP.put(context, userId);
	}
	
	/**
	 *  ע���û�ͨ������
	 */
	public void ungisterUserContext(ChannelHandlerContext context ){
		if(context  != null){
			int userId = CHANNEL_USER_MAP.getOrDefault(context,0);
			CHANNEL_USER_MAP.remove(context);
			USER_CHANNEL_MAP.remove(userId);
			context.close();
		}
	}
	
}
