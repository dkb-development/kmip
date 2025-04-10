import inspect
from kmip.pie.client import ProxyKmipClient
from kmip.services.kmip_client import KMIPProxy

# Try to get the get method from ProxyKmipClient
print("=== ProxyKmipClient.get method ===")
for name, method in inspect.getmembers(ProxyKmipClient, predicate=inspect.isfunction):
    if name == "get":
        print(inspect.getsource(method))

# Try to get the get method from KMIPProxy
print("\n=== KMIPProxy.get method ===")
for name, method in inspect.getmembers(KMIPProxy, predicate=inspect.isfunction):
    if name == "get":
        print(inspect.getsource(method))
    elif name == "_get":
        print("\n=== KMIPProxy._get method ===")
        print(inspect.getsource(method))
