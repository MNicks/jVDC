package de.nikxs.digitalstrom.vdc.server;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import vdcapi.Messages;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Builder
@Accessors(fluent=true) @Getter
public final class Request {

    private final @NonNull int id;

    private final @NonNull Consumer<Messages.GenericResponse> callback;

    private final LocalDateTime timestamp = LocalDateTime.now();

}
