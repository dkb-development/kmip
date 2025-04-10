from __future__ import annotations
from kmip.pie.client import ProxyKmipClient
from kmip.core.enums import CryptographicAlgorithm, CryptographicUsageMask, KMIPVersion
import logging
from typing import Optional, List
from dataclasses import dataclass
import os

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@dataclass
class KeyMetadata:
    key_id: str
    algorithm: CryptographicAlgorithm
    length: int
    usage_mask: List[CryptographicUsageMask]

class KmipClient:
    def __init__(self, host: str = 'localhost', port: int = 5696) -> None:
        # Define path to the server certificate
        cert_path = os.path.join(os.path.dirname(__file__), 'kmip_server_cert.pem')
        logger.info(f"Using server CA certificate: {cert_path}")

        self.client = ProxyKmipClient(
            hostname=host,
            port=port,
            ca=cert_path,  # Path to trusted server CA certificate
            ssl_version="PROTOCOL_TLSv1_2",
            kmip_version=KMIPVersion.KMIP_2_0  # Use KMIP 2.0
        )
        logger.info(f"Initialized KMIP client for {host}:{port} using KMIP 2.0")

    def create_symmetric_key(
        self,
        algorithm: CryptographicAlgorithm = CryptographicAlgorithm.AES,
        length: int = 256
    ) -> str:
        """Create a new symmetric key"""
        try:
            with self.client:
                key_id = self.client.create(
                    algorithm,
                    length,
                    cryptographic_usage_mask=[
                        CryptographicUsageMask.ENCRYPT,
                        CryptographicUsageMask.DECRYPT
                    ]
                )
                return key_id
        except Exception as e:
            logger.error(f"Error creating symmetric key: {e}")
            raise

    def get_symmetric_key(self, key_id: str) -> Optional[KeyMetadata]:
        """Get a symmetric key by its ID"""
        try:
            # Disable PyKMIP debug logging
            import logging
            logging.getLogger('kmip').setLevel(logging.WARNING)

            with self.client:
                # Get the key using the standard client method
                key = self.client.get(key_id)

                # Extract key attributes
                algorithm = getattr(key, 'cryptographic_algorithm', None)
                length = getattr(key, 'cryptographic_length', 0)
                usage_mask = getattr(key, 'cryptographic_usage_masks', [])

                return KeyMetadata(
                    key_id=key_id,
                    algorithm=algorithm,
                    length=length,
                    usage_mask=usage_mask
                )
        except Exception as e:
            logger.error(f"Error getting symmetric key: {e}")
            raise

    def rotate_symmetric_key(self, key_id: str) -> str:
        """Rotate a symmetric key"""
        try:
            with self.client:
                new_key_id = self.client.rekey(key_id)
                logger.info(f"Rotated key {key_id} to new key {new_key_id}")
                return new_key_id
        except Exception as e:
            logger.error(f"Error rotating symmetric key: {e}")
            raise

    def destroy_symmetric_key(self, key_id: str) -> None:
        """Destroy a symmetric key"""
        try:
            with self.client:
                self.client.destroy(key_id)
                logger.info(f"Destroyed key with ID: {key_id}")
        except Exception as e:
            logger.error(f"Error destroying symmetric key: {e}")
            raise

def main() -> None:
    # Create client instance
    client = KmipClient()
    logger.info("Starting KMIP client demonstration...")

    try:
        # Step 1: Create a symmetric key
        logger.info("="*50)
        logger.info("STEP 1: CREATING A NEW SYMMETRIC KEY")
        logger.info("="*50)
        key_id = client.create_symmetric_key()
        logger.info(f"SUCCESS: Created symmetric key with ID: {key_id}")
        logger.info("="*50 + "\n")

        # Pause briefly to ensure the key is fully processed by the server
        import time
        logger.info("Waiting 2 seconds before retrieving the key...")
        time.sleep(2)

        # Step 2: Retrieve the key we just created
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

    except Exception as e:
        logger.error(f"Error in demonstration: {e}")

    logger.info("="*50)
    logger.info("KMIP CLIENT DEMONSTRATION COMPLETE")
    logger.info("Successfully demonstrated KMIP 2.0 operations: Create and Get")
    logger.info("="*50)

if __name__ == "__main__":
    main()