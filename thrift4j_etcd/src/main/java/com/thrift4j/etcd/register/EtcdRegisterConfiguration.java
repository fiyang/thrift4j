package com.thrift4j.etcd.register;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.thrift4j.etcd.configure.EtcdAutoConfiguration;
import com.thrift4j.etcd.configure.EtcdDiscoveryProperties;

import lombok.extern.slf4j.Slf4j;
import mousio.etcd4j.EtcdClient;

@Configuration
@EnableScheduling
@Import(EtcdAutoConfiguration.class)
@Slf4j
public class EtcdRegisterConfiguration {

	@Autowired
	private EtcdRegister etcdRegister;

	@Autowired
	private EtcdClient etcdClient;

	@Autowired
	private EtcdDiscoveryProperties etcdRegisterProperties;

	private static final Pattern DEFAULT_ADDRESS_PATTERN = Pattern.compile("\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})?");

	private boolean isRefresh = false;

	@Scheduled(initialDelayString = "${etcd.discovery.heartbeat:5000}", fixedRateString = "${spring.cloud.etcd.discovery.heartbeat:5000}")
	protected void sendHeartbeat() {
		register();
	}

	private void register() {
		if (!etcdRegister.isStart()) {
			return;
		}

		String registerPath = etcdRegister.getPath();
		String registerKey = etcdRegister.getKey();
		String value = etcdRegister.getValue();
		if (registerPath != null && registerKey != null && value != null
				&& DEFAULT_ADDRESS_PATTERN.matcher(registerKey).matches()) {

			String path = registerPath + "/" + registerKey;
			int sessionTime = etcdRegisterProperties.getTtl();
			try {
				if (isRefresh) {
					etcdClient.refresh(path, sessionTime).send().get();
				} else {
					etcdClient.put(path, value).ttl(sessionTime).send().get();
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					etcdClient.put(path, value).ttl(sessionTime).send().get();
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
			isRefresh = true;
		}
	}
}
