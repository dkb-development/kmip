package com.kmip.server.core.enums;

/**
 * Enumeration representing the various states of the KMIP server.
 * 
 * This enum helps track the server lifecycle and provides better
 * error handling and debugging capabilities.
 */
public enum ServerState {
    
    /**
     * Server is not initialized and not ready to accept connections.
     */
    NOT_INITIALIZED("Not Initialized", "Server has not been initialized"),
    
    /**
     * Server is in the process of initializing.
     */
    INITIALIZING("Initializing", "Server is starting up and initializing components"),
    
    /**
     * Server is initialized but not yet started.
     */
    INITIALIZED("Initialized", "Server is initialized but not started"),
    
    /**
     * Server is in the process of starting.
     */
    STARTING("Starting", "Server is starting and binding to port"),
    
    /**
     * Server is running and accepting connections.
     */
    RUNNING("Running", "Server is running and accepting client connections"),
    
    /**
     * Server is in the process of stopping.
     */
    STOPPING("Stopping", "Server is shutting down gracefully"),
    
    /**
     * Server has stopped and is no longer accepting connections.
     */
    STOPPED("Stopped", "Server has stopped and is not accepting connections"),
    
    /**
     * Server encountered an error and is in an error state.
     */
    ERROR("Error", "Server encountered an error and may not be functioning properly"),
    
    /**
     * Server is in maintenance mode (not accepting new connections).
     */
    MAINTENANCE("Maintenance", "Server is in maintenance mode");
    
    private final String displayName;
    private final String description;
    
    ServerState(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets the human-readable display name for this state.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the detailed description of this state.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if the server is in a state where it can accept connections.
     * 
     * @return true if the server can accept connections, false otherwise
     */
    public boolean canAcceptConnections() {
        return this == RUNNING;
    }
    
    /**
     * Checks if the server is in a transitional state.
     * 
     * @return true if the server is transitioning between states, false otherwise
     */
    public boolean isTransitional() {
        return this == INITIALIZING || this == STARTING || this == STOPPING;
    }
    
    /**
     * Checks if the server is in a stable state.
     * 
     * @return true if the server is in a stable state, false otherwise
     */
    public boolean isStable() {
        return !isTransitional();
    }
    
    /**
     * Checks if the server is operational (running or in maintenance).
     * 
     * @return true if the server is operational, false otherwise
     */
    public boolean isOperational() {
        return this == RUNNING || this == MAINTENANCE;
    }
    
    /**
     * Checks if the server is in an error state.
     * 
     * @return true if the server is in an error state, false otherwise
     */
    public boolean isError() {
        return this == ERROR;
    }
    
    /**
     * Gets the next expected state based on the current state and operation.
     * 
     * @param operation The operation being performed
     * @return The expected next state
     */
    public ServerState getNextState(String operation) {
        switch (this) {
            case NOT_INITIALIZED:
                if ("initialize".equals(operation)) {
                    return INITIALIZING;
                }
                break;
            case INITIALIZING:
                if ("complete".equals(operation)) {
                    return INITIALIZED;
                } else if ("error".equals(operation)) {
                    return ERROR;
                }
                break;
            case INITIALIZED:
                if ("start".equals(operation)) {
                    return STARTING;
                }
                break;
            case STARTING:
                if ("complete".equals(operation)) {
                    return RUNNING;
                } else if ("error".equals(operation)) {
                    return ERROR;
                }
                break;
            case RUNNING:
                if ("stop".equals(operation)) {
                    return STOPPING;
                } else if ("maintenance".equals(operation)) {
                    return MAINTENANCE;
                } else if ("error".equals(operation)) {
                    return ERROR;
                }
                break;
            case MAINTENANCE:
                if ("resume".equals(operation)) {
                    return RUNNING;
                } else if ("stop".equals(operation)) {
                    return STOPPING;
                }
                break;
            case STOPPING:
                if ("complete".equals(operation)) {
                    return STOPPED;
                } else if ("error".equals(operation)) {
                    return ERROR;
                }
                break;
            case ERROR:
                if ("reset".equals(operation)) {
                    return NOT_INITIALIZED;
                }
                break;
        }
        return this; // No state change
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
