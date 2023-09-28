/*
Copyright (C) 2023 e:fs TechHub GmbH (sdk@efs-techhub.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.efs.sdk.search.commons;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class SearchException extends Exception {

    @Serial
    private static final long serialVersionUID = 8515681965419030997L;

    private final HttpStatus httpStatus;
    private final int errorCode;

    public SearchException(SEARCH_ERROR error) {
        super(error.code + ": " + error.msg);
        httpStatus = error.status;
        errorCode = error.code;
    }

    public SearchException(SEARCH_ERROR error, String additionalMessage) {
        super(error.code + ": " + error.msg + " " + additionalMessage);
        httpStatus = error.status;
        errorCode = error.code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getErrorCode() {
        return errorCode;
    }


    /**
     * Provides the errors to the application.
     *
     * @author e:fs TechHub GmbH
     */
    public enum SEARCH_ERROR {
        // @formatter:off
        EXTRACTION_ERROR(10001, HttpStatus.UNPROCESSABLE_ENTITY, "error parsing search-result"),
        SEARCH_FAILED(10002, HttpStatus.UNPROCESSABLE_ENTITY, "error sending request against elasticsearch."),
        UNKNOWN_OPERATOR(10003, HttpStatus.NOT_FOUND, "unknown operator"),
        ERROR_CREATING_QUERY(10004, HttpStatus.UNPROCESSABLE_ENTITY, "error creating query"),
        INVALID_BETWEEN_FILTER_MISSING_UPPERBOUND(10008, HttpStatus.BAD_REQUEST, "unable to build between-filter, missing upper-bound"),
        INVALID_BETWEEN_FILTER_MISSING_LOWERBOUND(10009, HttpStatus.BAD_REQUEST, "unable to build between-filter, missing lower-bound"),
        INVALID_FILTER_MISSING_VALUE(10010, HttpStatus.BAD_REQUEST, "unable to build filter, missing value"),
        UNAUTHORIZED(10012, HttpStatus.UNAUTHORIZED, "no authentication-header found"),
        UNABLE_EXTRACT_RETURN_VALUE(10013, HttpStatus.UNPROCESSABLE_ENTITY, "unable to extract elasticsearch-return-value"),
        UNABLE_EXTRACT_STRING_TO_OBJECT(10014, HttpStatus.UNPROCESSABLE_ENTITY, "unable to extract string to object:"),
        UNABLE_GET_ES_CLIENT(10015, HttpStatus.INTERNAL_SERVER_ERROR, "unable to connect to elasticsearch. Try again later..."),
        EXTRACTION_ERROR_MAPPING(10020, HttpStatus.UNPROCESSABLE_ENTITY, "error parsing mapping-result"),
        UNKNOWN_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "something unexpected happened.");
        // @formatter:on


        private final int code;
        private final HttpStatus status;
        private final String msg;

        SEARCH_ERROR(int code, HttpStatus status, String msg) {
            this.code = code;
            this.status = status;
            this.msg = msg;
        }

    }
}
