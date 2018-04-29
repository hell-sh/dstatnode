package live.dstat.dstatnode;

import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;

public class NetworkMonitor extends Thread
{
	NetworkMonitor()
	{
		new Thread(this).start();
	}

	@Override
	public void run()
	{
		try
		{
			do
			{
				long up = 0;
				long down = 0;
				for(String ni : Main.sigar.getNetInterfaceList())
				{
					NetInterfaceStat netStat = Main.sigar.getNetInterfaceStat(ni);
					NetInterfaceConfig ifConfig = Main.sigar.getNetInterfaceConfig(ni);
					String hwaddr = null;
					if(!NetFlags.NULL_HWADDR.equals(ifConfig.getHwaddr()))
					{
						hwaddr = ifConfig.getHwaddr();
					}
					if(hwaddr != null)
					{
						up += netStat.getTxBytes();
						down += netStat.getRxBytes();
					}
				}
				synchronized(Main.sigar)
				{
					if(Main.down > 0)
					{
						Main.down = (down - Main._down);
					}
					Main._down = Main.down;
					if(Main.up > 0)
					{
						Main.up = (up - Main._up);
					}
					Main._up = Main.up;
					Main.requests = (Main.requests_ - Main._requests);
					Main._requests = Main.requests_;
				}
				Thread.sleep(1000);
			}
			while(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
