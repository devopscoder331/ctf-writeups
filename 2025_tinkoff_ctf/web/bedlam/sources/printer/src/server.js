import express from "express";
import { generateBoardingPass } from "./pdf.js";

const app = express();
app.use(express.urlencoded({ extended: true }));

app.get("/ping", (req, res) => {
    res.send("pong");
});

app.post("/", async (req, res) => {
    let boardingPassData = {
        passengerName: req.body.passenger,
        flightNumber: req.body.flight_number,
        date: req.body.date,
        seat: req.body.seat,
        from: req.body.from,
        to: req.body.to,
        departureTime: req.body.departure,
        arrivalTime: req.body.arrival,
        gate: req.body.gate,
        terminal: req.body.terminal,
        traceId: req.headers["x-trace-id"],
    };

    const requiredFields = [
        'passengerName', 'flightNumber', 'date', 'seat',
        'from', 'to', 'departureTime', 'arrivalTime',
        'gate', 'terminal'
    ];
    
    const missingFields = requiredFields.filter(field => !boardingPassData[field]);

    if (missingFields.length > 0) {
        return res.status(400).json({
            error: 'Missing required fields',
            missingFields
        });
    }

    try {
        const boardingPassPDF = await generateBoardingPass(boardingPassData);
        res.contentType("application/pdf");
        res.setHeader("Content-Disposition", "attachment; filename=boarding-pass.pdf");
        res.send(Buffer.from(boardingPassPDF));
        return;
    } catch (error) {
        console.error(`[${boardingPassData.traceId}] Error generating boarding pass: ${error}`);
        res.status(500).send("Error generating boarding pass");
        return;
    }
});
app.listen(3000, () => {
    console.log("Server is running on port 3000");
});
