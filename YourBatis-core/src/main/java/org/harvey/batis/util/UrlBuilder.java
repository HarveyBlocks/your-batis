package org.harvey.batis.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * 构建URL的工具类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-07 15:44
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class UrlBuilder {

    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";


    private String protocol;
    private String host;
    private Integer port;
    private String filepath;
    private Properties queryParameters;
    private String fragment;

    public static String queryParameters2String(Properties args) {
        if (args == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int size = args.size();
        for (Object key : args.keySet()) {
            String propertyName = (String) key;
            String propertyValue = args.getProperty(propertyName);
            builder.append(propertyName).append("=").append(propertyValue);
            if (--size != 0) {
                builder.append("&");
            }
        }
        return builder.toString();
    }

    public String file() {
        StringBuilder builder = new StringBuilder();
        if (filepath != null) {
            if (!filepath.startsWith("/")) {
                builder.append("/");
            }
            builder.append(filepath);
        }
        builder.append("?").append(UrlBuilder.queryParameters2String(queryParameters));
        return builder.toString();
    }

    public String authority() {
        if (port == null && host == null) {
            return null;
        }
        if (port == null) {
            return host;
        }
        if (host == null) {
            throw new IllegalStateException("host is null but port is " + port);
        }
        return host + ":" + port;
    }

    public String fileWithFragment() {
        StringBuilder builder = new StringBuilder(file());
        if (fragment != null) {
            if (!fragment.startsWith("#")) {
                builder.append("#");
            }
            builder.append(fragment);
        }
        return builder.toString();
    }

    public URL toUrl() throws MalformedURLException {
        return new URL(protocol, host, this.port, this.fileWithFragment());
    }

    @Override
    public String toString() {
        return protocol + "://" + this.authority() + this.fileWithFragment();
    }
}
