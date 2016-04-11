# Secure-Instant-Message-System

1. Architecture
Our proposed architecture is as shown below.
   a) client A login(aythentication) with Server and says I want to talk to client B
   b) Server make Key Establishment for client A and client B
   c) Client A communicate with Client B
   d) Client A log out

2. Protocols
   (1) Authentication protocol (login)
      a)	Request Login
      b) S -> A: Cookie - Hash(IPa, timestamp)	
      c)	A -> S: Cookie, {A}pks, g^a mod p
      d)	S -> A: g^b + g^w mod p, u, C1
      The shared key between client and server is K = g^b(a + uW) mod p 
      e) A -> S: K{c1}, c2	
      f)	S -> A: K{c2}

      1.	User A inputs the username and password into the workstation
      2.	The workstation sends a request login message to server
      3.	After receiving authentication request message from client, the server first hash IP address of A with timestamp           as a cookie to client. Since IP address information is included, DoS attack from this certain IP address can be            avoid.
      4.	Then client randomly generate ‘a’, and sends back server the cookie, A’s username encrypted with server’s public           key and . Then both client and server can compute the session key which is 
      5.	Client sends server  and a random number 
      6.	Server sends client  to fulfill the authentication that both client and server know the session key 

   (2) Key Establishment Protocol (and authentication with peers)
      a)	A -> S: Kas{A, B, N1} 
      b) S -> A: Kas{B, IPb, portb, N1, Kab, t-to-B}	
      c)	A -> B: t-to-B, Kab{N2}
      d) B -> A: Kab{N1 - 1, N3, g^b mod p}	
      e) A -> B: Kab{N3 - 1, g^a mod p}	
         Key used between A and B is K = g^ab mod p 

      1.	Client A sends to the server a request message to talk to B with a random number  and the whole message is              encrypted by session key between client A and server S.
      2.	The server then sends back a message which contains who A is going to talk to, B’s IP address, port number,  which       is sent from A,  which is used to establish communication between A and B and a ticket to B. The ticket contains  and       is encrypted using B’s secret key
      3.	Then A sends B the ticket to B and  as a challenge
      4.	If B admits the chat request from A, B will sends A  which is an answer to the previous challenge, number  as a         challenge to A and  to try to establish D-H
      5.	A sends  to answer B’s challenge and  to complete D-H

   We should note that session key between A and B is  instead of . This can work perfectly if A or B doesn’t trust the       server. Even if the server acts like man in the middle, he can only know  and  and knows nothing about the real session    key. And once  is established, A forgets a and B forgets b.

   (3) Messaging Protocol
      a) A -> B: Kab, {M1, HMAC(M1)}	
      b)	B -> A: Kab, {M2, HMAC(M2)}

      1.	When A wants to send a message to B, A will use  to encrypt the message and HMAC of that message
      2.	B does the same while B wants to send message to A

   (4) Logout Protocol
      a)	A -> S: Kas{FIN, N1}
      b)	S -> A: Kas{FIN - ACK, N1 - 1, N2}
      c)	A -> S: Kas{ACK, N2 - 1}

      1.	Once A wants to logout, A just send a message include FIN and challenge  encrypted using 
      2.	Server then sends back  and another challenge  encrypted using 
      3.	A sends back a ACK message as well as 

3. Issues
   a. Does your system protect against the use of weak passwords? Discuss both online and offline dictionary attacks. 
   It will work well for online attack. Because of timestamp, it will be time-out if the clients enter the password in a certain times attempts (maybe 3). 
   On the contrary, although we use the public key of the server to encrypt the client’s identity, which means that only the server can decrypt it by using its private key, it still can be decrypted by offline dictionary attacks.

   b. Is your design resistant to denial of service attacks? 
   Yes, because we have used cookies. According to our protocol, when there is a connection initiation request, server will send a timestamp to the IP address of request. Server will not compute anything until it receives the cookie sent from the source IP address. 
   What’s more, because of timestamp, it will time out in a short time.

   c. To what level does your system provide end-points hiding, or perfect forward 
   secrecy? 
   Any level of this architecture, the identity of the end point is not revealed in clear text and strong encryption will protect identities of all of clients. When the server is compromised, the identities of clients will be disclosed. But, after all, it is a rare case. And perfect forward secrecy is achieved by generating keys for each session. So even if the intruder gets the key, he can only decrypt information for that session.    
   
   d. If the users do not trust the server can you devise a scheme that prevents the 
   server from decrypting the communication between the users without requiring the users to remember more than a password? Discuss the cases when the user trusts (vs. does not trust) the application running on his workstation. 
   In our architecture, when the client A wants to initiate a connection with Client B, the server passes same tickets to Client A and Client B. Both the clients generate a common session key using the ticket. Here the server does not know the session key. This session key is used for interaction between the clients. Thus, none of the conversation is passed through the server and the trust factor is resolved.






