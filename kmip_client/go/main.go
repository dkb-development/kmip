package main

import (
	"crypto/tls"
	"fmt"
	"log"

	"github.com/gemalto/kmip-go/kmip14"
	"github.com/gemalto/kmip-go/ttlv"
)

func main() {
	// Create TLS config
	config := &tls.Config{
		InsecureSkipVerify: true, // For testing only
	}

	// Connect to server
	conn, err := tls.Dial("tcp", "localhost:5696", config)
	if err != nil {
		log.Fatalf("Failed to connect: %v", err)
	}
	defer conn.Close()

	// Create request
	req := ttlv.NewStructure(kmip14.Tag_RequestMessage)
	
	// Add request header
	header := ttlv.NewStructure(kmip14.Tag_RequestHeader)
	protocolVersion := ttlv.NewStructure(kmip14.Tag_ProtocolVersion)
	protocolVersion.AddInteger(kmip14.Tag_ProtocolVersionMajor, 2)
	protocolVersion.AddInteger(kmip14.Tag_ProtocolVersionMinor, 0)
	header.AddStructure(kmip14.Tag_ProtocolVersion, protocolVersion)
	header.AddInteger(kmip14.Tag_BatchCount, 1)
	req.AddStructure(kmip14.Tag_RequestHeader, header)

	// Add batch item
	batchItem := ttlv.NewStructure(kmip14.Tag_RequestBatchItem)
	batchItem.AddEnumeration(kmip14.Tag_Operation, kmip14.Operation_Create)

	// Add request payload
	payload := ttlv.NewStructure(kmip14.Tag_RequestPayload)
	payload.AddEnumeration(kmip14.Tag_ObjectType, kmip14.ObjectType_SymmetricKey)

	// Add template attribute
	templateAttr := ttlv.NewStructure(kmip14.Tag_TemplateAttribute)
	templateAttr.AddEnumeration(kmip14.Tag_CryptographicAlgorithm, kmip14.CryptographicAlgorithm_AES)
	templateAttr.AddInteger(kmip14.Tag_CryptographicLength, 256)
	templateAttr.AddInteger(kmip14.Tag_CryptographicUsageMask, 0x0C) // Encrypt/Decrypt
	payload.AddStructure(kmip14.Tag_TemplateAttribute, templateAttr)

	batchItem.AddStructure(kmip14.Tag_RequestPayload, payload)
	req.AddStructure(kmip14.Tag_RequestBatchItem, batchItem)

	// Encode and send request
	reqBytes, err := ttlv.Marshal(req)
	if err != nil {
		log.Fatalf("Failed to marshal request: %v", err)
	}

	_, err = conn.Write(reqBytes)
	if err != nil {
		log.Fatalf("Failed to send request: %v", err)
	}

	// Read response
	respBytes := make([]byte, 4096)
	n, err := conn.Read(respBytes)
	if err != nil {
		log.Fatalf("Failed to read response: %v", err)
	}

	// Decode response
	var resp ttlv.Value
	if err := ttlv.Unmarshal(respBytes[:n], &resp); err != nil {
		log.Fatalf("Failed to unmarshal response: %v", err)
	}

	// Print response
	fmt.Printf("Response: %s\n", ttlv.Sprint(resp))
} 