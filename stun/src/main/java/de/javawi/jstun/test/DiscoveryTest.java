/*
 * This file is part of JSTUN.
 *
 * Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
 * reserved.
 *
 * This software is licensed under either the GNU Public License (GPL),
 * or the Apache 2.0 license. Copies of both license agreements are
 * included in this distribution.
 */

package de.javawi.jstun.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeException;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

public class DiscoveryTest {

    InetAddress sourceIaddress;
    int sourcePort;
    String stunServer;
    int stunServerPort;
    int timeoutInitValue = 300; //ms
    MappedAddress ma = null;
    ChangedAddress ca = null;
    boolean nodeNatted = true;
    DatagramSocket socketTest1 = null;
    DiscoveryInfo di = null;

    public DiscoveryTest(InetAddress sourceIaddress, int sourcePort, String stunServer, int stunServerPort) {
        this.sourceIaddress = sourceIaddress;
        this.sourcePort = sourcePort;
        this.stunServer = stunServer;
        this.stunServerPort = stunServerPort;
    }

    public DiscoveryInfo test() throws UtilityException, IOException, MessageAttributeException, MessageHeaderParsingException {
        ma = null;
        ca = null;
        nodeNatted = true;
        socketTest1 = null;
        di = new DiscoveryInfo(sourceIaddress);

        if (test1()) {
            if (test2()) {
                if (test1Redo()) {
                    test3();
                }
            }
        }

        socketTest1.close();

        return di;
    }

    private boolean test1() throws UtilityException, IOException, MessageAttributeParsingException, MessageHeaderParsingException {
        int timeSinceFirstTransmission = 0;
        int timeout = timeoutInitValue;
        while (true) {
            try {
                // Test 1 including response
                socketTest1 = new DatagramSocket(new InetSocketAddress(sourceIaddress, sourcePort));
                System.out.println(socketTest1.getLocalPort());
                socketTest1.setReuseAddress(true);
                socketTest1.connect(InetAddress.getByName(stunServer), stunServerPort);
                socketTest1.setSoTimeout(timeout);

                MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
                sendMH.generateTransactionID();

                ChangeRequest changeRequest = new ChangeRequest();
                sendMH.addMessageAttribute(changeRequest);

                byte[] data = sendMH.getBytes();
                DatagramPacket send = new DatagramPacket(data, data.length);
                socketTest1.send(send);


                MessageHeader receiveMH = new MessageHeader();
                while (!(receiveMH.equalTransactionID(sendMH))) {
                    DatagramPacket receive = new DatagramPacket(new byte[200], 200);
                    socketTest1.receive(receive);
                    receiveMH = MessageHeader.parseHeader(receive.getData());
                    receiveMH.parseAttributes(receive.getData());
                }

                ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
                ca = (ChangedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
                ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
                if (ec != null) {
                    di.setError(ec.getResponseCode(), ec.getReason());

                    return false;
                }
                if ((ma == null) || (ca == null)) {
                    di.setError(700, "The server is sending an incomplete response (Mapped Address and Changed Address message attributes are missing). The client should not retry.");

                    return false;
                } else {
                    di.setPublicIP(ma.getAddress().getInetAddress());
                    di.setPublicPort(ma.getPort());
                    if ((ma.getPort() == socketTest1.getLocalPort()) && (ma.getAddress().getInetAddress().equals(socketTest1.getLocalAddress()))) {

                        nodeNatted = false;
                    } else {

                    }
                    return true;
                }
            } catch (SocketTimeoutException ste) {
                if (timeSinceFirstTransmission < 7900) {

                    timeSinceFirstTransmission += timeout;
                    int timeoutAddValue = (timeSinceFirstTransmission * 2);
                    if (timeoutAddValue > 1600) timeoutAddValue = 1600;
                    timeout = timeoutAddValue;
                } else {
                    // node is not capable of udp communication

                    di.setBlockedUDP();

                    return false;
                }
            } finally {
                if (socketTest1 != null) {
                    socketTest1.close();
                }
                socketTest1 = null;
            }
        }
    }

    private boolean test2() throws UtilityException, IOException, MessageAttributeException, MessageHeaderParsingException {
        int timeSinceFirstTransmission = 0;
        int timeout = timeoutInitValue;
        while (true) {
            try {
                // Test 2 including response
                DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(sourceIaddress, sourcePort));
                sendSocket.connect(InetAddress.getByName(stunServer), stunServerPort);
                sendSocket.setSoTimeout(timeout);

                MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
                sendMH.generateTransactionID();

                ChangeRequest changeRequest = new ChangeRequest();
                changeRequest.setChangeIP();
                changeRequest.setChangePort();
                sendMH.addMessageAttribute(changeRequest);

                byte[] data = sendMH.getBytes();
                DatagramPacket send = new DatagramPacket(data, data.length);
                sendSocket.send(send);


                int localPort = sendSocket.getLocalPort();
                InetAddress localAddress = sendSocket.getLocalAddress();

                sendSocket.close();

                DatagramSocket receiveSocket = new DatagramSocket(localPort, localAddress);
                receiveSocket.connect(ca.getAddress().getInetAddress(), ca.getPort());
                receiveSocket.setSoTimeout(timeout);

                MessageHeader receiveMH = new MessageHeader();
                while (!(receiveMH.equalTransactionID(sendMH))) {
                    DatagramPacket receive = new DatagramPacket(new byte[200], 200);
                    receiveSocket.receive(receive);
                    receiveMH = MessageHeader.parseHeader(receive.getData());
                    receiveMH.parseAttributes(receive.getData());
                }
                ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
                if (ec != null) {
                    di.setError(ec.getResponseCode(), ec.getReason());

                    return false;
                }
                if (!nodeNatted) {
                    di.setOpenAccess();

                } else {
                    di.setFullCone();

                }
                return false;
            } catch (SocketTimeoutException ste) {
                if (timeSinceFirstTransmission < 7900) {

                    timeSinceFirstTransmission += timeout;
                    int timeoutAddValue = (timeSinceFirstTransmission * 2);
                    if (timeoutAddValue > 1600) timeoutAddValue = 1600;
                    timeout = timeoutAddValue;
                } else {

                    if (!nodeNatted) {
                        di.setSymmetricUDPFirewall();

                        return false;
                    } else {
                        // not is natted
                        // redo test 1 with address and port as offered in the changed-address message attribute
                        return true;
                    }
                }
            }
        }
    }

    private boolean test1Redo() throws UtilityException, IOException, MessageAttributeParsingException, MessageHeaderParsingException {
        int timeSinceFirstTransmission = 0;
        int timeout = timeoutInitValue;
        while (true) {
            // redo test 1 with address and port as offered in the changed-address message attribute
            try {
                // Test 1 with changed port and address values
                socketTest1.connect(ca.getAddress().getInetAddress(), ca.getPort());
                socketTest1.setSoTimeout(timeout);

                MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
                sendMH.generateTransactionID();

                ChangeRequest changeRequest = new ChangeRequest();
                sendMH.addMessageAttribute(changeRequest);

                byte[] data = sendMH.getBytes();
                DatagramPacket send = new DatagramPacket(data, data.length);
                socketTest1.send(send);


                MessageHeader receiveMH = new MessageHeader();
                while (!(receiveMH.equalTransactionID(sendMH))) {
                    DatagramPacket receive = new DatagramPacket(new byte[200], 200);
                    socketTest1.receive(receive);
                    receiveMH = MessageHeader.parseHeader(receive.getData());
                    receiveMH.parseAttributes(receive.getData());
                }
                MappedAddress ma2 = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
                ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
                if (ec != null) {
                    di.setError(ec.getResponseCode(), ec.getReason());

                    return false;
                }
                if (ma2 == null) {
                    di.setError(700, "The server is sending an incomplete response (Mapped Address message attribute is missing). The client should not retry.");

                    return false;
                } else {
                    if ((ma.getPort() != ma2.getPort()) || (!(ma.getAddress().getInetAddress().equals(ma2.getAddress().getInetAddress())))) {
                        di.setSymmetric();

                        return false;
                    }
                }
                return true;
            } catch (SocketTimeoutException ste2) {
                if (timeSinceFirstTransmission < 7900) {

                    timeSinceFirstTransmission += timeout;
                    int timeoutAddValue = (timeSinceFirstTransmission * 2);
                    if (timeoutAddValue > 1600) timeoutAddValue = 1600;
                    timeout = timeoutAddValue;
                } else {

                    return false;
                }
            }
        }
    }

    private void test3() throws UtilityException, IOException, MessageAttributeException, MessageHeaderParsingException {
        int timeSinceFirstTransmission = 0;
        int timeout = timeoutInitValue;
        while (true) {
            try {
                // Test 3 including response
                DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(sourceIaddress, sourcePort));
                sendSocket.connect(InetAddress.getByName(stunServer), stunServerPort);
                sendSocket.setSoTimeout(timeout);

                MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
                sendMH.generateTransactionID();

                ChangeRequest changeRequest = new ChangeRequest();
                changeRequest.setChangePort();
                sendMH.addMessageAttribute(changeRequest);

                byte[] data = sendMH.getBytes();
                DatagramPacket send = new DatagramPacket(data, data.length);
                sendSocket.send(send);


                int localPort = sendSocket.getLocalPort();
                InetAddress localAddress = sendSocket.getLocalAddress();

                sendSocket.close();

                DatagramSocket receiveSocket = new DatagramSocket(localPort, localAddress);
                receiveSocket.connect(InetAddress.getByName(stunServer), ca.getPort());
                receiveSocket.setSoTimeout(timeout);

                MessageHeader receiveMH = new MessageHeader();
                while (!(receiveMH.equalTransactionID(sendMH))) {
                    DatagramPacket receive = new DatagramPacket(new byte[200], 200);
                    receiveSocket.receive(receive);
                    receiveMH = MessageHeader.parseHeader(receive.getData());
                    receiveMH.parseAttributes(receive.getData());
                }
                ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
                if (ec != null) {
                    di.setError(ec.getResponseCode(), ec.getReason());

                    return;
                }
                if (nodeNatted) {
                    di.setRestrictedCone();

                    return;
                }
            } catch (SocketTimeoutException ste) {
                if (timeSinceFirstTransmission < 7900) {

                    timeSinceFirstTransmission += timeout;
                    int timeoutAddValue = (timeSinceFirstTransmission * 2);
                    if (timeoutAddValue > 1600) timeoutAddValue = 1600;
                    timeout = timeoutAddValue;
                } else {

                    di.setPortRestrictedCone();

                    return;
                }
            }
        }
    }
}
