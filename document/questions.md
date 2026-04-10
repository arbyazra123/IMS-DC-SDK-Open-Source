# **IMS Data Channel Integration - Questions for Manufacturer**

## **BACKGROUND**

We are developing an Android SDK that enables data transmission during 5G phone calls (IMS Data Channel). Our SDK runs on Android OS and needs to communicate with the 5G modem chip to create and manage data channels. Since we are new to IMS Data Channel technology, we need your support to implement the connection layer between our SDK and your hardware platform.

**Our reference:** https://github.com/GSMATerminals/IMS-DC-SDK-Open-Source

---

## **1. TECHNICAL CAPABILITY**

**1.1 IMS Data Channel Support**

Does your current modem/chipset support IMS Data Channel functionality? If yes, which modem chipset and firmware version do you use? If not, is this feature under development, and when is the expected completion date?

**1.2 Interface Implementation**

Can you implement the required software interfaces for IMS Data Channel communication between our SDK and your hardware? We will provide the complete technical specifications for these interfaces. Please specify which programming language you will use (C++ or Java/Kotlin) and where the implementation files will be located on the device.

---

## **2. DOCUMENTATION REQUIREMENTS**

**2.1 What We Will Provide**

What technical documentation format do you require from us? We can provide interface specifications, API reference documents, integration guides, sequence diagrams, and test cases. Please specify your preferred document format (PDF, Word, Markdown, etc.) and if you have a standard specification template we should follow.

**2.2 What You Will Provide**

What documentation will you provide to us? Please specify the deliverables including installation guides, configuration manuals, API implementation details, error code references, and troubleshooting guides. Will you also provide reference implementation or sample code?

---

## **3. SOFTWARE DELIVERABLES**

**3.1 Implementation Files**

What are the file names, file paths (location on device), and approximate file sizes of the software libraries you will deliver? For example, where will the `.so` files be located (`/vendor/lib64/` or other paths)?

---

## **4. TESTING & DEVELOPMENT**

**4.1 Development Environment**

Can you provide a development device with IMS Data Channel support for testing? Which device models are available for testing, and how will the test environment be provided?

**4.2 Debugging Tools**

Do you provide debugging or logging tools to help troubleshoot integration issues? If yes, please specify the tool names and how to access them.

---

## **5. TIMELINE & DEVICE SUPPORT**

**5.1 Device Models**

Which device models will support this feature? Please list the specific model names and their expected availability dates. Will existing devices receive this feature via OTA update, or is it only for new devices?

---

## **6. TECHNICAL SPECIFICATIONS**

**6.1 Performance Limits**

What are the performance specifications and limitations:
- Maximum data transmission rate (kbps or Mbps)
- Maximum message size per transmission (KB)
- Maximum number of concurrent data channels per call
- Typical transmission latency (milliseconds)

**6.2 System Requirements**

What are the minimum requirements:
- Minimum Android version required
- Required Android permissions
- Special system configuration needed
- Memory, CPU, or power constraints

**6.3 Known Limitations**

Are there any known limitations or restrictions we should be aware of? This could include memory limitations, network restrictions, or other technical constraints.

---

## **9. SECURITY**

**9.1 Encryption & Security**

How is data channel encryption handled? What encryption standard is used?

**9.2 Security Requirements**

Are there security requirements from your side, such as signature verification, permission management, or SELinux policy modifications?

**9.3 Privacy Compliance**

What regulatory compliance is supported (GDPR, Chinese data protection laws, etc.)? What data privacy mechanisms are in place?

---

## **10. DEPENDENCIES & INTEGRATION**

**10.1 System Dependencies**

Are there any dependencies we should be aware of, such as specific IMS Core vendors, required system services, or third-party libraries?

**10.2 Known Issues**

Are there any known issues or limitations in your current IMS Data Channel implementation that we should consider during development?

**10.3 Distribution Method**

How can our SDK be distributed on your devices? Can it be pre-installed on devices, or must users download it from an app store? Are there any partnership requirements?

---
