import inspect
from kmip.core.messages.payloads.get import GetResponsePayload
from kmip.core.objects import KeyBlock
from kmip.core.secrets import SymmetricKey

# Examine the GetResponsePayload class
print("=== GetResponsePayload class ===")
print(inspect.getsource(GetResponsePayload))

# Examine the SymmetricKey class
print("\n=== SymmetricKey class ===")
print(inspect.getsource(SymmetricKey))

# Examine the KeyBlock class
print("\n=== KeyBlock class ===")
print(inspect.getsource(KeyBlock))
