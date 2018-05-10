package live.dstat.dstatnode;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import live.dstat.dstatnode.ws.WebSocketServerInitializer;
import org.hyperic.sigar.Sigar;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main
{
	public static final Sigar sigar = new Sigar();
	public static final String endpoint = "http://dstat.live/node/";
	public static String version = "1.1.1";
	public static String ipv4 = "";
	public static String ipv6 = "";
	public static long down = 0;
	public static long _down = 0;
	public static long up = 0;
	public static long _up = 0;
	public static long requests = 0;
	public static long requests_ = 0;
	public static long _requests = 0;

	public static void main(String[] args) throws Exception
	{
		System.out.println("     _        _            _         _   _             \n" +
				"  __| |  ___ | |_   __ _  | |_      | | (_) __ __  ___ \n" +
				" / _` | (_-< |  _| / _` | |  _|  _  | | | | \\ V / / -_)\n" +
				" \\__,_| /__/  \\__| \\__,_|  \\__| (_) |_| |_|  \\_/  \\___|\n\ndstatnode v" + version + "\n");
		int port = 80;
		String argKey = "";
		for(String arg : args)
		{
			if(!argKey.equals(""))
			{
				switch(argKey)
				{
					case "--port":
						port = Integer.parseInt(arg);
				}
				argKey = "";
			}
			else if(arg.startsWith("--"))
			{
				switch(arg)
				{
					case "--port":
						argKey = arg;
						break;
					case "--help":
						System.out.println("--help           Shows this help.");
						System.out.println("--port <port>    Binds this dstat node to the given port. (Default: 80)");
						return;
					default:
						System.out.println("Unknown argument: " + arg + "\nTry --help for a list of arguments.");
						return;
				}
			}
		}
		String info;
		final File providerFile = new File("info.txt");
		if(providerFile.exists())
		{
			BufferedReader reader = new BufferedReader(new FileReader(providerFile));
			info = reader.readLine();
			reader.close();
		}
		else
		{
			System.out.println("Please provide information about the server provider, DDoS Protection, etc.\nThis will be public. You have up to 128 characters.");
			do
			{
				System.out.print("> ");
				info = new Scanner(System.in).useDelimiter("\n").next();
				if(info.length() == 0)
				{
					System.out.println("Sorry, you have to provide information.");
				}
				else if(info.length() > 128)
				{
					System.out.println("Sorry, that's longer than 128 characters. Try again.");
				}
				else
				{
					break;
				}
			}
			while(true);
			System.out.println("Thank you. Feel free to edit the info.txt if anything changes.\n");
			PrintWriter writer = new PrintWriter("info.txt", "UTF-8");
			writer.print(info);
			writer.close();
		}
		System.out.print("IPv4: ");
		try
		{
			ipv4 = request("http://ip.nex.li/insecure-ipv4-director");
		}
		catch(Exception ignored)
		{
		}
		if(ipv4.equals(""))
		{
			System.out.println("Unable to determine");
		}
		else
		{
			System.out.println(ipv4);
		}
		System.out.print("IPv6: ");
		try
		{
			ipv6 = request("http://ip.nex.li/insecure-ipv6-director");
		}
		catch(Exception ignored)
		{
		}
		if(ipv6.equals(""))
		{
			System.out.println("Unable to determine");
			if(ipv4.equals(""))
			{
				System.out.println("Apparently you're not connected via IPv4 nor via IPv6?!");
				return;
			}
		}
		else
		{
			System.out.println(ipv6);
		}
		System.out.println("\nBinding to port " + port + "...");
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try
		{
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new WebSocketServerInitializer());
			Channel ch = b.bind(port).sync().channel();
			new NetworkMonitor();
			synchronized(sigar)
			{
				String response = request(Main.endpoint + "init", "version=" + URLEncoder.encode(version, "UTF-8") + "&ipv4=" + URLEncoder.encode(ipv4, "UTF-8") + "&ipv6=" + URLEncoder.encode(ipv6, "UTF-8") + "&port=" + URLEncoder.encode(String.valueOf(port), "UTF-8") + "&info=" + URLEncoder.encode(info, "UTF-8"));
				if(response.equals("update required"))
				{
					System.out.println("Sorry, your dstatnode version is too old. Please download the newest version from https://dstat.live");
					return;
				}
				else if(response.equals(""))
				{
					System.out.println("Sorry, there was an error setting up your node.\nPlease create an issue at: https://github.com/hellshltd/dstatnode/issues/new");
					return;
				}
				System.out.println("Thank you. Your node is live at https://dstat.live/" + response);
			}
			new Reporter();
			ch.closeFuture().sync();
		}
		catch(Exception e)
		{
			//noinspection ConstantConditions
			if(e instanceof BindException)
			{
				System.out.println("Unable to bind to port " + port + ". Make sure it's unused or change it using --port");
			}
			else
			{
				e.printStackTrace();
			}
		}
		finally
		{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static String request(String rawurl) throws Exception
	{
		URL url = new URL(rawurl);
		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection) con;
		http.setRequestMethod("GET");
		http.setDoOutput(true);
		http.setRequestProperty("User-Agent", "DstatNode");
		http.connect();
		return new Scanner(http.getInputStream()).useDelimiter("\\A").next();
	}

	public static String request(String rawurl, String data) throws Exception
	{
		URL url = new URL(rawurl);
		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection) con;
		http.setRequestMethod("POST");
		http.setDoOutput(true);
		//System.out.println("> " + data);
		byte[] out = data.getBytes(StandardCharsets.UTF_8);
		con.setRequestProperty("Accept", "*/*");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Content-Length", String.valueOf(data.length()));
		http.setRequestProperty("User-Agent", "DstatNode");
		http.connect();
		try(OutputStream os = http.getOutputStream())
		{
			os.write(out);
		}
		String response = "";
		Scanner sc = new Scanner(http.getInputStream()).useDelimiter("\\A");
		if(sc.hasNext())
		{
			response = sc.next();
		}
		//System.out.println("< " + response);
		return response;
	}
}
