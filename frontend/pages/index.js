import { useState } from "react";
import getConfig from 'next/config'

const { publicRuntimeConfig } = getConfig()

const Index = () => {
    const [inputs, setInputs] = useState({
        email: "",
        firstName: "",
        lastName: "",
        cardNumber: "",
        paymentId: "",
        moneyAmount: "",
        order: [
            {
                name: "",
                amount: ""
            }
        ]
    });

    const [jobs, setJobs] = useState([])

    function handleChange(event) {
        const { name, value } = event.target;
        setInputs((prevInputs) => ({ ...prevInputs, [name]: value }));
    }

    function handleDynamicInput(event, index) {
        const { name, value } = event.target;
        const orderValue = [...inputs.order];

        if (name.startsWith("name")) {
            orderValue.at(index).name = value;
        } else {
            orderValue.at(index).amount = value;
        }

        setInputs((prevInputs) => ({ ...prevInputs, order: orderValue }));
    }

    function handleNewInput(event) {
        const newOrder = [...inputs.order, { name: "", amount: "" }]
        setInputs((prevInputs) => ({ ...prevInputs, order: newOrder }));
    }

    async function handleSubmit(event) {
        const payload = {
            id: null,
            payment: {
                cardNumber: inputs.cardNumber,
                paymentId: inputs.paymentId,
                amount: Number(inputs.moneyAmount)
            },
            order: inputs.order.map(elem => ({ name: elem.name, amount: Number(elem.amount) })),
            user: {
                email: inputs.email,
                firstName: inputs.firstName,
                lastName: inputs.lastName
            }
        }

        const response = await fetch(`${publicRuntimeConfig.apiUrl}/shopping`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const responsePayload = await response.json();

        const job = {
            id: responsePayload.id,
            userStatus: "submitted",
            orderStatus: "submitted",
            paymentStatus: "submitted"
        }

        setJobs(prevJobs => [...prevJobs, job])
    }

    async function handleRefresh(event, job) {
        const response = await fetch(`${publicRuntimeConfig.apiUrl}/shopping/${job.id}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        const responsePayload = await response.json()

        const newJobs = jobs.map(jobIter => {
            if (jobIter.id === job.id) {
                return {
                    id: job.id,
                    userStatus: responsePayload.userStatus,
                    orderStatus: responsePayload.orderStatus,
                    paymentStatus: responsePayload.paymentStatus
                };
            }
            return jobIter;
        })

        setJobs(newJobs);
    }

    function handleColor(status) {
        switch (status) {
            case 'completed':
                return 'text-success';
            case 'failed':
                return 'text-danger';
            default:
                return 'text-body';
        }
    }

    return (
        <div className="mx-auto my-5">
            <div className="container-lg">
                <h1 className="p-3 mb-4 bg-primary bg-gradient text-white">Online Shopping Platform</h1>
                <form>
                    <div className="mb-5">
                        <h2>User Registration</h2>
                        <div className="mb-3">
                            <label htmlFor="email" className="form-label">Email address</label>
                            <input type="email" className="form-control" id="email" name="email" value={inputs.email} onChange={e => handleChange(e)} />
                        </div>
                        <div className="mb-3">
                            <label htmlFor="firstName" className="form-label">First name</label>
                            <input type="text" className="form-control" id="firstName" name="firstName" value={inputs.firstName} onChange={e => handleChange(e)} />
                        </div>
                        <div className="mb-3">
                            <label htmlFor="lastName" className="form-label">Last name</label>
                            <input type="text" className="form-control" id="lastName" name="lastName" value={inputs.lastName} onChange={e => handleChange(e)} />
                        </div>
                    </div>

                    <div className="mb-5">
                        <h2>Your Order</h2>
                        {
                            inputs.order.map((elem, i) =>
                                <div className="mb-3" key={i}>
                                    <div className="d-flex gap-5 justify-content-center align-items-end">
                                        <div className="w-100">
                                            <label htmlFor={"name" + i} className="form-label">Product name</label>
                                            <input type="text" className="form-control" id={"name" + i} name={"name" + i} value={inputs.order.at(i).name} onChange={e => handleDynamicInput(e, i)} />
                                        </div>

                                        <div className="w-100">
                                            <label htmlFor={"amount" + i} className="form-label">Amount</label>
                                            <input type="text" className="form-control" id={"amount" + i} name={"amount" + i} value={inputs.order.at(i).amount} onChange={e => handleDynamicInput(e, i)} />
                                        </div>
                                    </div>
                                </div>
                            )
                        }
                        <div className="my-4">
                            <button type="button" className="btn btn-primary" onClick={e => handleNewInput(e)}>Add</button>
                        </div>
                    </div>

                    <div className="mb-5">
                        <h2>Payment details</h2>
                        <div className="mb-3">
                            <label htmlFor="cardNumber" className="form-label">Card number</label>
                            <input type="cardNumber" className="form-control" id="cardNumber" name="cardNumber" value={inputs.cardNumber} onChange={e => handleChange(e)} />
                        </div>
                        <div className="mb-3">
                            <label htmlFor="paymentId" className="form-label">Payment ID</label>
                            <input type="text" className="form-control" id="paymentId" name="paymentId" value={inputs.paymentId} onChange={e => handleChange(e)} />
                        </div>
                        <div className="mb-3">
                            <label htmlFor="moneyAmount" className="form-label">Money amount</label>
                            <input type="text" className="form-control" id="moneyAmount" name="moneyAmount" value={inputs.moneyAmount} onChange={e => handleChange(e)} />
                        </div>
                    </div>

                    <button type="button" className="btn btn-primary w-100 bg-gradient py-2" onClick={(e) => handleSubmit(e)}>Submit</button>
                </form>

                <table className="table my-5 table-striped">
                    <thead>
                        <tr>
                            <th>No.</th>
                            <th>Job ID</th>
                            <th>User Registration Status</th>
                            <th>Order Status</th>
                            <th>Payment Status</th>
                            <th>Refresh</th>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            jobs.map((job, i) =>
                                <tr key={job.id}>
                                    <td>{i + 1}</td>
                                    <td>{job.id}</td>
                                    <td className={handleColor(job.userStatus)}>{job.userStatus}</td>
                                    <td className={handleColor(job.orderStatus)}>{job.orderStatus}</td>
                                    <td className={handleColor(job.paymentStatus)}>{job.paymentStatus}</td>
                                    <td><button type="button" className="btn btn-primary" onClick={e => handleRefresh(e, job)}>Refresh</button></td>
                                </tr>
                            )
                        }
                    </tbody>
                </table>
            </div >
        </div >
    )
}

export default Index;