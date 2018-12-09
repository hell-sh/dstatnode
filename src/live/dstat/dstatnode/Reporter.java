package live.dstat.dstatnode;

import java.net.URLEncoder;

public class Reporter extends Thread
{
	Reporter()
	{
		this.start();
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
					Main.request(Main.endpoint + "report", "down=" + URLEncoder.encode(String.valueOf(Main.down), "UTF-8") + "&up=" + URLEncoder.encode(String.valueOf(Main.up), "UTF-8") + "&requests=" + URLEncoder.encode(String.valueOf(Main.requests), "UTF-8"));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		while(!this.isInterrupted());
	}
}
