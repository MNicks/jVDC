package de.nikxs.digitalstrom.vdc.util;

import lombok.extern.slf4j.Slf4j;
import vdcapi.Messages;

@Slf4j
public class DsUtil {

    public static Messages.Message buildGenericResponse(Messages.ResultCode code, int messageId) {
        Messages.GenericResponse.Builder genericResponse = Messages.GenericResponse.newBuilder().setCode(code);
        switch (code) {
            case ERR_OK:
                genericResponse.setDescription("OK");
                break;
            case ERR_MESSAGE_UNKNOWN:
                genericResponse.setDescription("Unknown message type");
                break;
            case ERR_INCOMPATIBLE_API:
                genericResponse.setDescription("Incompatible or not given API version");
                break;
            case ERR_SERVICE_NOT_AVAILABLE:
                genericResponse.setDescription("Service not available");
                break;
            case ERR_INSUFFICIENT_STORAGE:
                genericResponse.setDescription("Insufficient storage");
                break;
            case ERR_FORBIDDEN:
                genericResponse.setDescription("Forbidden");
                break;
            case ERR_NOT_IMPLEMENTED:
                genericResponse.setDescription("Not implemented");
                break;
            case ERR_NO_CONTENT_FOR_ARRAY:
                genericResponse.setDescription("No content for array");
                break;
            case ERR_INVALID_VALUE_TYPE:
                genericResponse.setDescription("Invalid or unexpected value type");
                break;
            case ERR_MISSING_SUBMESSAGE:
                genericResponse.setDescription("Missing protocol submessage");
                break;
            case ERR_MISSING_DATA:
                genericResponse.setDescription("Missing data / empty message");
                break;
            case ERR_NOT_FOUND:
                genericResponse.setDescription("Requested entity was not found");
                break;
            case ERR_NOT_AUTHORIZED:
                genericResponse.setDescription("Not authorized to perform requested action");
                break;
            default:
                log.error("unhandled error code: {}", code.name());
        }

        return Messages.Message.newBuilder()
                .setType(Messages.Type.GENERIC_RESPONSE)
                .setMessageId(messageId)
                .setGenericResponse(genericResponse)
                .build();
    }

    public static boolean isValidMessageType(Messages.Message message, Messages.Type type) {
        if(message == null || type == null) {
            log.info("Unexpected message type. Expected type: {} ; Given type: {}", type.name(), message.getType().name());
            return false;
        }

        return message.getType().equals(type) ? true : false;
    }
}
