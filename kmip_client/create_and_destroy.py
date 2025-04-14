from kmip_client import KmipClient
import logging
import time

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def create_and_destroy_key():
    """
    Demonstrates the full key lifecycle:
    1. Create a symmetric key
    2. Retrieve the key to verify it exists
    3. Destroy the key
    4. Verify the key is destroyed
    """
    client = KmipClient()
    logger.info("Starting key lifecycle demonstration...")
    
    try:
        # Step 1: Create a symmetric key
        logger.info("="*50)
        logger.info("STEP 1: CREATING A NEW SYMMETRIC KEY")
        logger.info("="*50)
        key_id = client.create_symmetric_key()
        logger.info(f"SUCCESS: Created symmetric key with ID: {key_id}")
        logger.info("="*50 + "\n")
        
        # Wait briefly to ensure the key is fully processed
        logger.info("Waiting 2 seconds before retrieving the key...")
        time.sleep(2)
        
        # Step 2: Retrieve the key to verify it exists
        logger.info("="*50)
        logger.info("STEP 2: RETRIEVING THE SYMMETRIC KEY")
        logger.info("="*50)
        key_metadata = client.get_symmetric_key(key_id)
        
        if key_metadata:
            logger.info("SUCCESS: Retrieved key with the following metadata:")
            logger.info(f"  Key ID: {key_metadata.key_id}")
            logger.info(f"  Algorithm: {key_metadata.algorithm}")
            logger.info(f"  Length: {key_metadata.length} bits")
            logger.info(f"  Usage Mask: {key_metadata.usage_mask}")
            logger.info("="*50 + "\n")
        
        # Step 3: Destroy the key
        logger.info("="*50)
        logger.info("STEP 3: DESTROYING THE SYMMETRIC KEY")
        logger.info("="*50)
        client.destroy_symmetric_key(key_id)
        logger.info(f"SUCCESS: Destroyed symmetric key with ID: {key_id}")
        logger.info("="*50 + "\n")
        
        # Step 4: Verify the key is destroyed
        logger.info("="*50)
        logger.info("STEP 4: VERIFYING KEY DESTRUCTION")
        logger.info("="*50)
        try:
            client.get_symmetric_key(key_id)
            logger.error(f"ERROR: Key with ID {key_id} still exists after destruction!")
            return False
        except Exception as e:
            logger.info(f"SUCCESS: Key with ID {key_id} no longer accessible (Expected error: {e})")
            logger.info("="*50 + "\n")
            return True
            
    except Exception as e:
        logger.error(f"Error in demonstration: {e}")
        return False

if __name__ == "__main__":
    result = create_and_destroy_key()
    
    if result:
        logger.info("="*50)
        logger.info("KEY LIFECYCLE DEMONSTRATION COMPLETE")
        logger.info("Successfully demonstrated KMIP 2.0 operations: Create, Get, and Destroy")
        logger.info("="*50)
    else:
        logger.error("="*50)
        logger.error("KEY LIFECYCLE DEMONSTRATION FAILED")
        logger.error("="*50)
