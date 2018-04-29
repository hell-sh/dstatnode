package live.dstat.dstatnode;

import java.net.URLEncoder;

public class Reporter extends Thread
{
	Reporter()
	{
		new Thread(this).start();
	}

	@Override
	public void run()
	{
		do
		{
			try
			{
				Thread.sleep(30000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			try
			{
				synchronized(Main.sigar)
				{
					Main.request(Main.endpoint + "report", "ipv4=" + URLEncoder.encode(Main.ipv4, "UTF-8") + "&ipv6=" + URLEncoder.encode(Main.ipv6, "UTF-8") + "&down=" + URLEncoder.encode(String.valueOf(Main.down), "UTF-8") + "&up=" + URLEncoder.encode(String.valueOf(Main.up), "UTF-8") + "&requests=" + URLEncoder.encode(String.valueOf(Main.requests), "UTF-8"));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		while(true);
	}
}
