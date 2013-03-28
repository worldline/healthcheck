package net.atos.xa.healthcheck.openejb;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.hibernate.ejb.EntityManagerFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.HealthCheck;

public class MissingDatasourceCheck extends HealthCheck {

	/** the logger */
	private static Logger log = LoggerFactory
			.getLogger(MissingDatasourceCheck.class.getName());

	private static final String ROOT = "";

	public MissingDatasourceCheck() {
		super("missingDatasource");
	}

	@Override
	protected Result check() throws Exception {

		return null;
	}

	private List<String> getMissingDataSources(Context context, String prefix) {

		List<String> missingDatasources = null;

		try {
			NamingEnumeration<Binding> ne = context.listBindings(ROOT);

			while (ne.hasMoreElements()) {
				Binding current = ne.next();
				Object obj = current.getObject();
				if (obj instanceof Context) {
					List<String> datasources = getMissingDataSources(
							(Context) obj, prefix + '/' + current.getName());

					if (datasources != null) {
						if (missingDatasources == null)
							missingDatasources = new ArrayList<String>();
						missingDatasources.addAll(datasources);
					}

				} else if (obj instanceof EntityManagerFactoryImpl) {

					EntityManagerFactoryImpl entityFactory = (EntityManagerFactoryImpl) obj;

					log.debug("Add datasource checker for : {}",
							current.getName());

				}
			}

		} catch (NamingException e) {
			log.error(e.getMessage(), e);
			return null;
		}

		return missingDatasources;
	}
}
