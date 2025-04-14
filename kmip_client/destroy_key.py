from kmip_client import KmipClient
import logging
import sys

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def destroy_key(key_id):
    """
    Destroy a key with the given ID.
    
    Args:
        key_id (str): The ID of the key to destroy
        
    Returns:
        bool: True if the key was successfully destroyed, False otherwise
    """
    client = KmipClient()
    
    try:
        # First, verify the key exists
        logger.info(f"Verifying key {key_id} exists before destruction...")
        try:
            key_metadata = client.get_symmetric_key(key_id)
            if key_metadata:
                logger.info(f"Found key with ID: {key_id}")
                logger.info(f"  Algorithm: {key_metadata.algorithm}")
                logger.info(f"  Length: {key_metadata.length} bits")
        except Exception as e:
            logger.error(f"Key with ID {key_id} not found: {e}")
            return False
            
        # Destroy the key
        logger.info(f"Destroying key with ID: {key_id}")
        client.destroy_symmetric_key(key_id)
        logger.info(f"Key destroyed successfully: {key_id}")
        
        # Verify the key is destroyed
        logger.info(f"Verifying key {key_id} is no longer accessible...")
        try:
            client.get_symmetric_key(key_id)
            logger.error(f"ERROR: Key with ID {key_id} still exists after destruction!")
            return False
        except Exception as e:
            logger.info(f"SUCCESS: Key with ID {key_id} no longer accessible (Expected error: {e})")
            return True
            
    except Exception as e:
        logger.error(f"Error during key destruction process: {e}")
        return False

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python destroy_key.py <key_id>")
        sys.exit(1)
        
    key_id = sys.argv[1]
    result = destroy_key(key_id)
    
    if result:
        print(f"Key {key_id} was successfully destroyed.")
        sys.exit(0)
    else:
        print(f"Failed to destroy key {key_id}.")
        sys.exit(1)
