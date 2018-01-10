package de.nikxs.digitalstrom.vdc.config;

import de.nikxs.digitalstrom.vdc.util.DSUID;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class DSUIDConverter implements Converter<String, DSUID> {
    @Override
    public DSUID convert(String source) {
        if(source==null){
            return null;
        }

        return DSUID.fromDSUID(source);
    }
}
