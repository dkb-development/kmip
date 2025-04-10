import inspect
from kmip.core.factories import secrets
from kmip.core import enums
from kmip.core.secrets import SymmetricKey

# Print the source code of the SecretFactory class
print("=== SecretFactory Source Code ===")
print(inspect.getsource(secrets.SecretFactory))

# Print the source code of the SymmetricKey class
print("\n=== SymmetricKey Source Code ===")
print(inspect.getsource(SymmetricKey))

# Create a SymmetricKey instance and print its tag
print("\n=== SymmetricKey Tag ===")
symmetric_key = SymmetricKey()
print(f"SymmetricKey tag: {symmetric_key.tag}")
print(f"SymmetricKey tag hex: 0x{symmetric_key.tag.value:X}")

# Print all tags from the Tags enumeration
print("\n=== All Tags ===")
for name, value in vars(enums.Tags).items():
    if not name.startswith('_') and hasattr(value, 'value'):
        print(f"{name}: 0x{value.value:X}")

# Print the object types
print("\n=== Object Types ===")
for name, value in vars(enums.ObjectType).items():
    if not name.startswith('_') and hasattr(value, 'value'):
        print(f"{name}: {value.value}")

# Create a secret factory and print what it creates for a symmetric key
print("\n=== Secret Factory Create ===")
factory = secrets.SecretFactory()
secret = factory.create(enums.ObjectType.SYMMETRIC_KEY)
print(f"Created secret type: {type(secret)}")
print(f"Created secret tag: {secret.tag}")
print(f"Created secret tag hex: 0x{secret.tag.value:X}")
