/*
 * Copyright 2025-China Telecom Research Institute.
 * All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ct.ertclib.dc.feature.testing.socket

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.net.NetworkInterface
import java.net.Inet4Address

class HotspotIpHelper(private val context: Context) {

    /**
     * 获取热点IP地址
     */
    fun getHotspotIpAddress(): String? {
        return try {
            // 方法1: 通过网络接口获取
            getHotspotIpFromInterfaces() ?:
            // 方法2: 通过WifiManager获取（备用）
            getHotspotIpFromWifiManager()
        } catch (e: Exception) {
            Log.e("HotspotIpHelper", "Error getting hotspot IP: ${e.message}")
            null
        }
    }

    /**
     * 通过遍历网络接口获取可用于P2P连接的IP（WiFi/热点局域网地址，或Tailscale等VPN覆盖网络地址）。
     * 不再限制接口名称包含"wlan"/"ap"/"softap"——Tailscale等VPN在Android上通常以tun接口出现，
     * 名称不匹配这些关键字，仅靠接口名过滤会在纯移动数据+VPN场景下永远找不到可用地址。
     * 改为遍历全部非回环IPv4接口，按地址段判断是否可用（局域网私有地址或Tailscale CGNAT段）。
     */
    private fun getHotspotIpFromInterfaces(): String? {
        return try {
            var fallback: String? = null
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        val ip = address.hostAddress ?: continue
                        if (isTailscaleIp(ip)) {
                            // Tailscale地址优先返回：纯移动数据场景下，这通常是唯一能被对端设备访问到的地址
                            return ip
                        }
                        if (isHotspotIp(ip) && fallback == null) {
                            fallback = ip
                        }
                    }
                }
            }
            fallback
        } catch (e: Exception) {
            Log.e("HotspotIpHelper", "Error getting IP from interfaces: ${e.message}")
            null
        }
    }

    /**
     * Tailscale（及其他基于CGNAT段的overlay网络）分配的地址段：100.64.0.0/10
     */
    private fun isTailscaleIp(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        val first = parts[0].toIntOrNull() ?: return false
        val second = parts[1].toIntOrNull() ?: return false
        return first == 100 && second in 64..127
    }

    /**
     * 通过WifiManager获取热点IP（需要系统权限或root）
     */
    private fun getHotspotIpFromWifiManager(): String? {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val method = wifiManager.javaClass.getDeclaredMethod("getWifiApConfiguration")
            method.isAccessible = true
            val config = method.invoke(wifiManager)

            if (config != null) {
                val ipAddressField = config.javaClass.getDeclaredField("ipAddress")
                ipAddressField.isAccessible = true
                val ipAddress = ipAddressField.getInt(config)

                // 将整型IP转换为字符串格式
                if (ipAddress != 0) {
                    return convertIntToIp(ipAddress)
                }
            }
            null
        } catch (e: Exception) {
            Log.e("HotspotIpHelper", "Error getting IP from WifiManager: ${e.message}")
            null
        }
    }

    /**
     * 判断是否为热点IP（私有地址范围）
     */
    private fun isHotspotIp(ip: String): Boolean {
        return when {
            ip.startsWith("192.168.") -> true
            ip.startsWith("10.") -> true
            ip.startsWith("172.") -> {
                val secondPart = ip.split(".")[1].toInt()
                secondPart in 16..31
            }
            else -> false
        }
    }

    /**
     * 将整型IP地址转换为字符串格式
     */
    private fun convertIntToIp(ip: Int): String {
        return "${(ip and 0xFF)}.${(ip shr 8 and 0xFF)}.${(ip shr 16 and 0xFF)}.${(ip shr 24 and 0xFF)}"
    }

    /**
     * 获取所有网络接口信息（用于调试）
     */
    fun getAllNetworkInterfaces(): List<String> {
        val interfaceInfo = mutableListOf<String>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val info = StringBuilder()
                info.append("Interface: ${networkInterface.name}\n")
                info.append("Display: ${networkInterface.displayName}\n")

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    info.append("  IP: ${address.hostAddress} (${address.javaClass.simpleName})\n")
                }

                interfaceInfo.add(info.toString())
            }
        } catch (e: Exception) {
            interfaceInfo.add("Error: ${e.message}")
        }
        return interfaceInfo
    }
}