package cmccss.service;

import java.util.Map;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.facebook.fb303.fb_status;
import com.thrift4j.server.annotation.ThriftService;

import cmccss.contract.User;
import cmccss.contract.UsesrService;

@ThriftService
@Component
public class UserServiceImpl implements UsesrService.Iface{

	@Override
	public long aliveSince() throws TException {
		return 0;
	}

	@Override
	public long getCounter(String arg0) throws TException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, Long> getCounters() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCpuProfile(int arg0) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOption(String arg0) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getOptions() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public fb_status getStatus() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatusDetails() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reinitialize() throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOption(String arg0, String arg1) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public User recommend(User request) throws TException {
		return null;
	}

}
