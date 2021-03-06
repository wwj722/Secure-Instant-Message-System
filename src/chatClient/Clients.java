                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ient.iv, 0, 16);
//		    System.out.println("iv iv iv !! "+new String(ChatClient.iv));
		    secondMessageDetail.userA = userName;
		    secondMessageDetail.pwd = cryption.encryptPublic(pwdHash, "server");
		    secondMessageDetail.gABModP = cryption.encryptPublic(dh.genPublicKey(), "server");
		    byte[] secondMessageDataByte = so.serialize(secondMessageDetail);
		    
		    Message secondMessage = new Message();
		    secondMessage.TYPE = "AUTHENTICATE";
		    secondMessage.cookie = cookie;
		    secondMessage.data = secondMessageDataByte;
			byte[] smBytes = so.serialize(secondMessage);
//			System.out.println("smBytes: "+smBytes.length);
			outStream.write(smBytes);
			
			// wait for server's public key
			InputStream inStream = authenticateSocket.getInputStream();
			inStream = authenticateSocket.getInputStream();
			Message auMessage = (Message) so.deserialize(ServiceMethods.readFully(inStream));
			if (auMessage.TYPE.equals("ERROR")) {
				System.out.println(new String(auMessage.data));
				return 1;
			}
			MessageDetail authentMsgDetail = (MessageDetail) so.deserialize(auMessage.data);
			byte[] gBModP = cryption.decryptPrivate(authentMsgDetail.gABModP, "client");
			byte[] decryptC1 = cryption.decryptPrivate(authentMsgDetail.c1, "client");
			int c1 = new BigInteger(decryptC1).intValue();
			byte[] aesKey = dh.genAESKey(gBModP).getEncoded();
//			System.out.println("c1: "+c1);
			
			// send K{c1}, c2
			byte[] KC1 = cryption.encrypt(aesKey, c1, ChatClient.iv);	
//			System.out.println("what!!! "+new BigInteger(cryption.decrypt(aesKey, KC1, ChatClient.iv)).intValue());
			int c2 = ServiceMethods.genRandom();
			MessageDetail fourthMsgDetail = new MessageDetail();
			fourthMsgDetail.KC = KC1;
			fourthMsgDetail.c2 = c2;
			Message fourthMessage = new Message();
			fourthMessage.TYPE = "FOURTH MESSAGE";
			fourthMessage.cookie = cookie;
			fourthMessage.data = so.serialize(fourthMsgDetail);
			outStream.write(so.serialize(fourthMessage));
			
			// wait for K{c2} from server
			Message lastMessage = (Message) so.deserialize(ServiceMethods.readFully(inStream));
			if (auMessage.TYPE.equals("ERROR")) {
				System.out.println(new String(auMessage.data));
				return 2;
			}
			MessageDetail lastMessageDetail = (MessageDetail) so.deserialize(lastMessage.data);
			byte[] decryptC2 = cryption.decrypt(aesKey, lastMessageDetail.c1, ChatClient.iv);
			
			if (c2 != (new BigInteger(decryptC2).intValue())) {
				System.out.println("Error: c2 in login part is not right");
				return 3;
			}
			int clientChatPort = lastMessageDetail.c2;
			ChatClient.chatPort = clientChatPort;
			ChatClient.aesKey = aesKey;
//			System.out.println("got from server chatPort: "+clientChatPort);
			
			outStream .close();
			inStream.close();
			authenticateSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return 0;
	}
	
	public String requestCookie(String userName, InetAddress serverAdd, int serverPort) {
		try {
			Message requestMessage = new Message();
			requestMessage.TYPE = "REQUEST LOGIN";
			
			SerializeObject so = new SerializeObject();
			byte[] requestByte = so.serialize(requestMessage);
			
			Socket loginSocket = new Socket(serverAdd, serverPort);
			OutputStream outStream = loginSocket.getOutputStream();
			outStream.write(requestByte);
			
			// wait for cookie
			InputStream inStream = loginSocket.getInputStream();
			Message cookieReply = (Message) so.deserialize(ServiceMethods.readFully(inStream));
			String cookie = cookieReply.cookie;
			ChatClient.cookie = cookie;
			ChatClient.userA = userName;
			
			loginSocket.close();
		    return cookie;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	public int keyEstablish (String peername, InetAddress serverAdd, int serverPort) {
		Socket keyEstabSocket;
		try {
			keyEstabSocket = new Socket(serverAdd, serverPort);
			OutputStream outStream =  keyEstabSocket.getOutputStream();
		    
			SerializeObject so = new SerializeObject();
			EncryptDecrypt cryption = new EncryptDecrypt();
		    Message ticketMessage = new Message();
		    ticketMessage.TYPE = "KEY ESTABLISHMENT";
		    ticketMessage.cookie = ChatClient.cookie;
		    
		    MessageDetail ticketMessageDetail = new MessageDetail();
		    ticketMessageDetail.userA = ChatClient.userA;
		    ticketMessageDetail.userB = peername;
		    int n1 = ServiceMethods.genRandom();
		    ticketMessageDetail.c2 = n1;
		    
		    
		    ticketMessage.data = cryption.encrypt(ChatClient.aesKey, so.serialize(ticketMessageDetail), ChatClient.iv);
		    outStream.write(so.serialize(ticketMessage));
//		    System.out.println("n1: "+n1);
		    
		    // wait for ticket message
		    InputStream inputStream = keyEstabSocket.getInputStream();
		    Message getTicket = (Message) so.deserialize(ServiceMethods.readFully(inputStream));
//		    System.out.println("getTicket.TYPE: "+getTicket.TYPE);
		    if (getTicket.TYPE.equals("ERROR")) {
		    	System.out.println(new String(getTicket.data));
				return 1;
		    }
		    byte [] ticketMsgByte = cryption.decrypt(ChatClient.aesKey, getTicket.data, ChatClient.iv);
		    MessageDetail ticketDetail = (MessageDetail) so.deserialize(ticketMsgByte);
		    if (ticketDetail.c2 != n1) {
		    	System.out.println("Error: c2 is not right in key establishment process");
		    	return 2; 
		    }
		    if (!ticketDetail.userB.equals(peername)) {
		    	System.out.println("Error: this is not the requested peer");
		    	return 3;
		    }
		    InetAddress ipB = ticketDetail.ip;
		    int port = ticketDetail.port;
		    byte[] KAB = ticketDetail.KAB;
//		    System.out.println(" byte[] KAB: "+new String(KAB));
		    byte[] iv = ticketDetail.c1;
		    byte[] ticket = ticketDetail.ticket;
		    
//		    System.out.println("ticket got!");
		    PeerInfo peerInfo = new PeerInfo();
		    peerInfo.KAB = KAB;
		    peerInfo.iv = iv;
		    peerInfo.ip = ipB;
		    peerInfo.port = port;
		    ChatClient.peers.put(peername, peerInfo);
		    
		    prepareCommunicate(ticket, peername);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public void prepareCommunicate(byte[] ticket, String peername) {
		// send ticket KAB{N2}
		int n2 = ServiceMethods.genRandom();
		ChatClient.peers.get(peername).num = n2;
//		System.out.println(n2);
		
		Message ticketMessage = new Message();
		EncryptDecrypt edDecrypt = new EncryptDecrypt();
		SerializeObject so = new SerializeObject();
		PeerInfo peerInfo = ChatClient.peers.get(peername);
		
		ticketMessage.TYPE = "REQUEST TALK";
		ticketMessage.ticket = ticket;
		ticketMessage.data = edDecrypt.encrypt(peerInfo.KAB, n2, peerInfo.iv);
		byte[] sendData = so.serialize(ticketMessage);
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, peerInfo.ip, peerInfo.port);
//		System.out.println("send packet port: "+peerInfo.port);
		try {
			IncommingChatThread.chatSocket.send(sendPacket);
			
//			byte[] receiveData = new byte[65536];
//			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//			IncommingChatThread.chatSocket.receive(receivePacket);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void responseTalkRequest(Message message, InetAddress ip, int port) {
		String peername = getNameByAdd(ip, port);
		PeerInfo peerInfo = ChatClient.peers.get(peername);
		EncryptDecrypt ed = new EncryptDecrypt();
		SerializeObject so = new SerializeObject();
		
		byte[] decryptReply = ed.decrypt(peerInfo.KAB, message.data, ChatClient.iv);
		MessageDetail replyDetail = (MessageDetail) so.deserialize(decryptReply);
		if (replyDetail.c2 != peerInfo.num-1) {
			ChatClient.peers.remove(peername);
			return;
		}
		int n3 = replyDetail.c3;
		byte[] gBModP = replyDetail.gABModP;
		
		DiffieHellman dh = new DiffieHellman();
		byte[] gAModP = dh.genPublicKey();
		MessageDetail lastMessageDetail = new MessageDetail();
		lastMessageDetail.gABModP = gAModP;
		lastMessageDetail.c3 = n3-1;
		byte[] encryptedData = ed.encrypt(peerInfo.KAB, so.serialize(lastMessageDetail), peerInfo.iv);
//		System.out.println("peerInfo.KAB peerInfo.KAB peerInfo.KAB "+new String(peerInfo.KAB));
//		System.out.println("peerInfo.iv peerInfo.iv peerInfo.iv: "+new String(peerInfo.iv));
		
		Message lastMessage = new Message();
		lastMessage.TYPE = "KEY GOT";
		lastMessage.data = encryptedData;
		byte[] sendData = so.serialize(lastMessage);
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, peerInfo.ip, peerInfo.port);
		try {
			IncommingChatThread.chatSocket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		
		// update the common key to DH key
		byte[] updatedKAB = dh.genAESKey(gBModP).getEncoded();
		ChatClient.peers.get(peername).KAB = updatedKAB;
		ChatClient.peers.get(peername).authenticated = true;
//		System.out.println(ChatClient.peers.size());
//		System.out.println(ChatClient.peers.get(peername));
//		System.out.println("finish common key establishment!");
	}
	
	public void acceptTalk (Message ticketMessage, InetAddress ip, int port) {
		byte[] ticket = ticketMessage.ticket;
		byte[] KN2 = ticketMessage.data;
		
		EncryptDecrypt edDecrypt = new EncryptDecrypt();
		SerializeObject so = new SerializeObject();
		byte[] decryptTicDetail = edDecrypt.decrypt(ChatClient.aesKey, ticket, ChatClient.iv);
		MessageDetail ticketDetail = (MessageDetail) so.deserialize(decryptTicDetail);
		long expire = ticketDetail.expire;
		if (expire < System.currentTimeMillis()) {
			System.out.println("ticket expired!");
			return;
		}
		String username = ticketDetail.userA;
		byte[] KAB = ticketDetail.KAB;
		PeerInfo peerInfo = new PeerInfo();
		peerInfo.KAB = KAB;
		peerInfo.iv = ticketDetail.c1;
		peerInfo.ip = ip;
		peerInfo.port = port;
		
		
		int n2 = new BigInteger(edDecrypt.decrypt(KAB, KN2, ChatClient.iv)).intValue();
		int n3 = ServiceMethods.genRandom();
		
		DiffieHellman dh = new DiffieHellman();
		byte[] gBModP = dh.genPublicKey();
		
		Message replyMessage = new Message();
		replyMessage.TYPE = "REPLY TALK REQUEST";
		MessageDetail replyDetail = new MessageDetail();
		replyDetail.c2 = n2-1;
		replyDetail.c3 = n3;
		
		peerInfo.num = n3;
		peerInfo.dh = dh;
		ChatClient.peers.put(username, peerInfo);
		replyDetail.gABModP = gBModP;
		// tell the opposite who am I
//		replyDetail.userB = ChatClient.userA;
		replyMessage.data = edDecrypt.encrypt(KAB, so.serialize(replyDetail), peerInfo.iv);
		
		try {
			byte[] sendData = so.serialize(replyMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
			IncommingChatThread.chatSocket.send(sendPacket);
		
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void acceptKeyBuild(Message message, InetAddress ip, int port) {
		String peername = getNameByAdd(ip, port);
		PeerInfo peerInfo = ChatClient.peers.get(peername);
		byte[] KAB = peerInfo.KAB;
		
		EncryptDecrypt ed = new EncryptDecrypt();
		SerializeObject so = new SerializeObject();
		
		byte[] lastMsgByte = ed.decrypt(KAB, message.data, ChatClient.iv);
		MessageDetail lastMessageDetail = (MessageDetail) so.deserialize(lastMsgByte);
		if (lastMessageDetail.c3 != peerInfo.num-1) {
			System.out.println("n3 does not match.");
			ChatClient.peers.remove(peername); 
		}
		byte[] gAModP = lastMessageDetail.gABModP;
		byte[] updatedKAB = peerInfo.dh.genAESKey(gAModP).getEncoded();
		ChatClient.peers.get(peername).KAB = updatedKAB;
		ChatClient.peers.get(peername).authenticated = true;
//		System.out.println(ChatClient.peers.size());
//		System.out.println(ChatClient.peers.get(peername));
//		System.out.println("finish common key establishment!");
	}
	
	/**
	 * This function is used for communication between clients
	 * @param userName
	 */
	public void communicate(String userName, String content) {
		PeerInfo peerInfo = ChatClient.peers.get(userName);
		byte[] KAB = peerInfo.KAB;
		InetAddress ip = peerInfo.ip;
		int port = peerInfo.port;
		byte[] peerIV= peerInfo.iv;

		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(KAB, "HmacSHA256");
			sha256_HMAC.init(secret_key);
			byte[] hmac = sha256_HMAC.doFinal(content.getBytes());
			
			SerializeObject so = new SerializeObject();
			EncryptDecrypt ed = new EncryptDecrypt();
			
			Message message = new Message();
			message.TYPE = "TALK";
			MessageDetail messageDetail = new MessageDetail();
			messageDetail.userA = content;
			messageDetail.ticket = hmac;
			message.data = ed.encrypt(KAB, so.serialize(messageDetail), peerIV);
			
			byte[] sendData = so.serialize(message);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
			IncommingChatThread.chatSocket.send(sendPacket);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void printMessage(Message message, InetAddress ip, int port) {
		String peername = getNameByAdd(ip, port);
//		System.out.println(peername);
		PeerInfo peerInfo = ChatClient.peers.get(peername);
		if (!peerInfo.authenticated) {
			System.out.println("authentication is not finished.");
			return;
		}
		
		byte[] KAB = peerInfo.KAB;
		byte[] iv = peerInfo.iv;
		EncryptDecrypt eDecrypt = new EncryptDecrypt();
		SerializeObject so = new SerializeObject();
		MessageDetail messageDetail = (MessageDetail) so.deserialize(eDecrypt.decrypt(KAB, message.data, iv));
		
		String content = messageDetail.userA;
		byte[] receivedhmac = messageDetail.ticket;
		
		Mac sha256_HMAC;
		try {
			sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(KAB, "HmacSHA256");
			sha256_HMAC.init(secret_key);
			byte[] hmac = sha256_HMAC.doFinal(content.getBytes());
			
			if (!Arrays.equals(receivedhmac, hmac)) {
				System.out.println("Digest of the message is not right");
				return;
			}
			
			System.out.println("message from "+peername+": "+content);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getNameByAdd(InetAddress ip, int port) {
//		System.out.println("peers.size(): "+ChatClient.peers.size());
//		System.out.println("talk ip: "+ip.getAddress());
//		System.out.println("talk port: "+port);
		HashMap<String, PeerInfo> peers = ChatClient.peers;
		for (Map.Entry<String, PeerInfo> entry : peers.entrySet()) {
			byte[] ipLocal = entry.getValue().ip.getAddress();
			
//			System.out.println(new String(ipLocal));
//			System.out.println(entry.getValue().port);
			if (Arrays.equals(ipLocal, ip.getAddress())) {
				if (entry.getValue().port == port)
					return entry.getKey();
			}
		}
		 return null;
	}
	
	/**
	 * Request for the list of online clients except for himself
	 * @return
	 */
	public void getOnlineList(InetAddress ip, int port) {
		Message message = new Message();
		message.TYPE = "LIST PEERS";
		message.cookie = ChatClient.cookie;
		
		try {
			Socket socket = new Socket(ip, port);
			SerializeObject so = new SerializeObject();
			byte[] msgByte = so.serialize(message);
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(msgByte);
			
			InputStream inputStream = socket.getInputStream();
			Message replyMessage = (Message) so.deserialize(ServiceMethods.readFully(inputStream));
			if (!replyMessage.TYPE.equals("LIST REPLY")) {
				System.out.println("messge should be list reply but it seems not");
				return;
			}
			EncryptDecrypt ed = new EncryptDecrypt();
			String onlineUser = new String(ed.decrypt(ChatClient.aesKey, replyMessage.data, ChatClient.iv));
			System.out.println("online users are: "+onlineUser);
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This function is called when the client need to log out
	 */
	public void logout(InetAddress ip, int port) {
		Message message = new Message();
		message.TYPE = "FIN";
		message.cookie = ChatClient.cookie;
		MessageDetail msgDetail = new MessageDetail();
		int n1 = ServiceMethods.genRandom();
		msgDetail.c2 = n1;
		SerializeObject so = new SerializeObject();
		EncryptDecrypt ed = new EncryptDecrypt();
		message.data = ed.encrypt(ChatClient.aesKey, so.serialize(msgDetail), ChatClient.iv);
		
		Socket socket;
		try {
			socket = new Socket(ip, port);
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(so.serialize(message));
			
			InputStream inputStream = socket.getInputStream();
			Message replyMessage = (Message) so.deserialize(ServiceMethods.readFully(inputStream));
			
			if (!replyMessage.TYPE.equals("FIN-ACK")) {
				System.out.println("message is not FIN-ACK type");
				return;
			}
			MessageDetail replyDetail = (MessageDetail) so.deserialize(ed.decrypt(ChatClient.aesKey, replyMessage.data, ChatClient.iv));
//			System.out.println("my n1 is "+n1);
//			System.out.println(replyDetail.c2);
			if (replyDetail.c2 != n1-1) {
				System.out.println("n1 is not right in the logout message");
				return;
			}
			int n2 = replyDetail.c3;
			
			Message finMessage = new Message();
			finMessage.TYPE = "ACK";
			finMessage.cookie = ChatClient.cookie;
			finMessage.data = ed.encrypt(ChatClient.aesKey, n2-1, ChatClient.iv);
			outputStream.write(so.serialize(finMessage));
			
			// clear all the data, read line stops
			ChatClient.aesKey = null;
			ChatClient.chatPort = -1;
			ChatClient.cookie = null;
			ChatClient.iv = null;
			ChatClient.peers = null;
			ChatClient.userA = null;
			System.out.println("I'm successfully loged out");
			
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
