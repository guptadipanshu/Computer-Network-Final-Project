package com.javapapers.java.gcm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

@WebServlet("/GCMNotification")
public class GCMNotification extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String GOOGLE_SERVER_KEY = "AIzaSyDDesQQChLKJvYbC18hD6Tnw9KKvu39U3E";
	static final String MESSAGE_KEY = "message";	
	 Map<String, String> paramsMap = new HashMap<String, String>();
	public GCMNotification() 
	{
		super();
	}

	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException 
	{

		System.out.println("doGet:");
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException 
	{
		
		
		Result result = null;
		String method=request.getMethod();
		System.out.println("request: " +method);
		// GCM RedgId of Android device to send push notification
		 
		String regId = "";
		regId = request.getParameter("regId");
		if (regId != null && !regId.isEmpty()) 
		{
			String from =request.getParameter("from"); 
			paramsMap.put(from, regId);
			String to =request.getParameter("to"); 
			String sender_key="";
			sender_key=paramsMap.get(to);
			String userMessage;
			if(sender_key=="")
			{
				sender_key=regId;
				userMessage="Please invite your friend to TrackChat";
			}
			else
			{
				userMessage=request.getParameter("message");
				userMessage+=","+request.getParameter("latlong");
				userMessage+=","+request.getParameter("name");
			}
			System.out.println("REGiD= " +regId +"from="+from +"msg "+ userMessage);
			try
			{
				Sender sender = new Sender(GOOGLE_SERVER_KEY);
				Message message = new Message.Builder().timeToLive(3)
						.delayWhileIdle(true).addData("message", userMessage).build();
				result = sender.send(message, sender_key, 1);
				request.setAttribute("pushStatus", result.toString());
				System.out.println("result: " + result.toString());
			}catch (IOException ioe) 
			{
				ioe.printStackTrace();
				request.setAttribute("pushStatus",
						"RegId required: " + ioe.toString());
			}
			request.setAttribute("pushStatus", "GCM RegId Received.");
			request.getRequestDispatcher("index.jsp").forward(request, response);
		} 

		else{	
			
			System.out.println("Else: regId not found");
		}
	}
}
