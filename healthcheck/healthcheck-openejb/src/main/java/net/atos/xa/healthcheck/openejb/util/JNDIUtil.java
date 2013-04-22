package net.atos.xa.healthcheck.openejb.util;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JNDIUtil {

	/** the logger */
	private static Logger log = LoggerFactory.getLogger(JNDIUtil.class
			.getName());

	private static final String ROOT = "";

	private JNDIUtil() {

	}

	public static void appendJndiTreeToBuffer(StringBuffer buffer,
			Context context, String prefix) {
		try {
			NamingEnumeration<Binding> ne = context.listBindings(ROOT);

			while (ne.hasMoreElements()) {
				Binding current = ne.next();
				Object obj = current.getObject();
				if (obj instanceof Context) {
					appendJndiTreeToBuffer(buffer, (Context) obj, prefix + '/'
							+ current.getName());

				} else {

					if (prefix != null) {
						buffer.append(prefix + "/" + current.getName() + "("
								+ obj.getClass() + ")\n");
					}

					else {
						buffer.append(current.getName() + "(" + obj.getClass()
								+ ")\n");
					}
				}

			}

		} catch (NamingException e) {
			log.debug(e.getMessage(), e);

		}
	}

	public static Context getOpenEjbRootContext() {
		return SystemInstance.get().getComponent(ContainerSystem.class)
				.getJNDIContext();
	}

}
