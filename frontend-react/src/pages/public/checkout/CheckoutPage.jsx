import React, { useState, useEffect, useRef } from 'react';
import { Container, Form, Button, Card, Row, Col, Alert } from 'react-bootstrap';
import axiosClient from '../../../api/axiosClient';
import { Client } from '@stomp/stompjs';

const CheckoutPage = () => {
    const [formData, setFormData] = useState({
        passengerTicketId: '',
        cargoTicketId: '',
        amount: '',
        paymentMethod: 'bank_transfer'
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [qrUrl, setQrUrl] = useState('');
    const [checkoutResult, setCheckoutResult] = useState(null);
    const [timeLeft, setTimeLeft] = useState(0);
    const [status, setStatus] = useState('idle'); // idle, pending, completed, cancelled
    const stompClientRef = useRef(null);

    useEffect(() => {
        return () => {
            if (stompClientRef.current) {
                stompClientRef.current.deactivate();
            }
        };
    }, []);

    useEffect(() => {
        let interval = null;
        if (status === 'PENDING') {
            interval = setInterval(() => {
                setTimeLeft((prev) => {
                    if (prev <= 1) {
                        clearInterval(interval);
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);
        }
        return () => {
            if (interval) clearInterval(interval);
        };
    }, [status]);

    useEffect(() => {
        if (status === 'PENDING' && timeLeft === 0 && checkoutResult && formData.paymentMethod === 'bank_transfer') {
            handleTimeout();
        }
    }, [timeLeft, status, checkoutResult]);

    const handleTimeout = () => {
        if (stompClientRef.current) {
            stompClientRef.current.deactivate();
        }
        setStatus('FAILED');
    };

    const formatTime = (seconds) => {
        const m = Math.floor(seconds / 60);
        const s = seconds % 60;
        return `${m}:${s < 10 ? '0' : ''}${s}`;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setQrUrl('');
        setCheckoutResult(null);

        try {
            const payload = {
                passengerTicketId: formData.passengerTicketId ? parseInt(formData.passengerTicketId) : null,
                cargoTicketId: formData.cargoTicketId ? parseInt(formData.cargoTicketId) : null,
                amount: parseFloat(formData.amount),
                paymentMethod: formData.paymentMethod
            };

            const response = await axiosClient.post('/payment/checkout', payload);
            setCheckoutResult(response);
            setStatus(response.status);

            if (formData.paymentMethod === 'bank_transfer' && response.amount && response.status === 'PENDING') {
                setQrUrl(`https://qr.sepay.vn/img?bank=Vietcombank&acc=SBSEPAYHCNTZK98PS6F&template=compact&amount=${response.amount}&des=${response.transactionId}`);

                setTimeLeft(300); // 5 minutes

                const baseUrl = axiosClient.defaults.baseURL || 'https://localhost:9090/api';
                const wsUrl = baseUrl.replace('http', 'ws').replace('/api', '/ws-payment');

                const client = new Client({
                    brokerURL: wsUrl,
                    onConnect: () => {
                        client.subscribe(`/topic/payment/${response.transactionId}`, (message) => {
                            const paymentData = JSON.parse(message.body);
                            setStatus(paymentData.status);
                            if (paymentData.status === 'COMPLETED' || paymentData.status === 'FAILED') {
                                setTimeLeft(0);
                                client.deactivate();
                            }
                        });
                    },
                    onStompError: (frame) => {
                        console.error('Broker reported error: ' + frame.headers['message']);
                        console.error('Additional details: ' + frame.body);
                    }
                });

                client.activate();
                stompClientRef.current = client;
            }
        } catch (err) {
            console.error("Checkout error:", err);
            setError(err?.response?.data?.message || err.message || 'An error occurred during checkout.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="py-5">
            <Row className="justify-content-center">
                <Col md={8} lg={6}>
                    <Card className="shadow-sm">
                        <Card.Body>
                            <h2 className="text-center mb-4">Checkout</h2>
                            {error && <Alert variant="danger">{error}</Alert>}
                            <Form onSubmit={handleSubmit}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Passenger Ticket ID</Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="passengerTicketId"
                                        value={formData.passengerTicketId}
                                        onChange={handleChange}
                                        placeholder="Enter Passenger Ticket ID"
                                    />
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label>Cargo Ticket ID</Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="cargoTicketId"
                                        value={formData.cargoTicketId}
                                        onChange={handleChange}
                                        placeholder="Enter Cargo Ticket ID"
                                    />
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label>Amount <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="amount"
                                        value={formData.amount}
                                        onChange={handleChange}
                                        required
                                        placeholder="Enter Amount"
                                    />
                                </Form.Group>

                                <Form.Group className="mb-4">
                                    <Form.Label>Payment Method <span className="text-danger">*</span></Form.Label>
                                    <Form.Select
                                        name="paymentMethod"
                                        value={formData.paymentMethod}
                                        onChange={handleChange}
                                        required
                                    >
                                        <option value="bank_transfer">Bank Transfer</option>
                                        <option value="cash">Cash</option>
                                    </Form.Select>
                                </Form.Group>

                                <Button
                                    variant="primary"
                                    type="submit"
                                    className="w-100"
                                    disabled={loading}
                                >
                                    {loading ? 'Processing...' : 'Checkout'}
                                </Button>
                            </Form>

                            {qrUrl && formData.paymentMethod === 'bank_transfer' && status === 'PENDING' && (
                                <div className="mt-5 text-center">
                                    <h4 className="mb-3">Scan QR to Pay</h4>
                                    <img src={qrUrl} alt="Vietcombank QR Code" className="img-fluid rounded border p-2" style={{ maxWidth: '300px' }} />
                                    {checkoutResult && (
                                        <p className="mt-3 text-muted">Transaction ID: {checkoutResult.transactionId}</p>
                                    )}
                                    <div className="mt-3">
                                        <h5 className="text-danger">Time remaining: {formatTime(timeLeft)}</h5>
                                    </div>
                                </div>
                            )}

                            {status === 'COMPLETED' && formData.paymentMethod === 'bank_transfer' && (
                                <Alert variant="success" className="mt-4">
                                    Payment successful! Transaction ID: {checkoutResult.transactionId}.
                                </Alert>
                            )}

                            {status === 'FAILED' && formData.paymentMethod === 'bank_transfer' && (
                                <Alert variant="danger" className="mt-4">
                                    Payment time expired and was cancelled.
                                </Alert>
                            )}

                            {status === 'COMPLETED' && formData.paymentMethod === 'cash' && (
                                <Alert variant="success" className="mt-4">
                                    Checkout successful! Transaction ID: {checkoutResult.transactionId}. Please pay with cash.
                                </Alert>
                            )}
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default CheckoutPage;
