#!/usr/bin/env node

/**
 * HTML Presentation to PowerPoint Converter
 *
 * This script:
 * 1. Opens the HTML presentation in a headless browser
 * 2. Screenshots each slide
 * 3. Creates a PowerPoint with the screenshots
 *
 * Requirements:
 *   npm install puppeteer pptxgenjs
 *
 * Usage:
 *   node html-to-pptx.js
 */

const puppeteer = require('puppeteer');
const PptxGenJS = require('pptxgenjs');
const path = require('path');
const fs = require('fs');

async function convertHTMLtoPPTX() {
    console.log('🚀 Starting HTML to PPTX conversion...');

    // Launch browser
    const browser = await puppeteer.launch({
        headless: true,
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });

    const page = await browser.newPage();

    // Set viewport to standard presentation size (16:9)
    await page.setViewport({
        width: 1920,
        height: 1080,
        deviceScaleFactor: 2 // Higher quality
    });

    // Load the HTML file
    const htmlPath = path.join(__dirname, 'TELKOMSEL_PRESENTATION.html');
    await page.goto(`file://${htmlPath}`, { waitUntil: 'networkidle0' });

    // Wait for presentation to load
    await page.waitForSelector('.slide.active');

    // Hide controls and counter for clean screenshots
    await page.evaluate(() => {
        document.querySelector('.controls').style.display = 'none';
        document.querySelector('.slide-counter').style.display = 'none';
    });

    // Get total number of slides
    const totalSlides = await page.evaluate(() => {
        return document.querySelectorAll('.slide').length;
    });

    console.log(`📊 Found ${totalSlides} slides`);

    // Create PowerPoint
    const pptx = new PptxGenJS();
    pptx.layout = 'LAYOUT_16x9';
    pptx.author = 'CODE-ID';
    pptx.title = 'IMS Data Channel Integration - Telkomsel';
    pptx.subject = '5G New Calling Phase 2 Design Plan';

    // Screenshot each slide
    const screenshotPaths = [];
    for (let i = 0; i < totalSlides; i++) {
        console.log(`📸 Capturing slide ${i + 1}/${totalSlides}...`);

        // Navigate to slide
        if (i > 0) {
            await page.evaluate(() => {
                window.nextSlide();
            });
            await new Promise(resolve => setTimeout(resolve, 500)); // Wait for animation
        }

        // Get the slide element's bounding box
        const slideElement = await page.$('.slide.active');
        const boundingBox = await slideElement.boundingBox();

        // Screenshot just the slide content
        const screenshotPath = path.join(__dirname, `temp-slide-${i}.png`);
        await page.screenshot({
            path: screenshotPath,
            clip: {
                x: boundingBox.x,
                y: boundingBox.y,
                width: boundingBox.width,
                height: boundingBox.height
            }
        });

        screenshotPaths.push(screenshotPath);
    }

    // Close browser
    await browser.close();

    // Add all screenshots to PowerPoint
    console.log('📦 Creating PowerPoint...');
    for (let i = 0; i < screenshotPaths.length; i++) {
        const slide = pptx.addSlide();
        slide.background = { color: 'FFFFFF' };
        slide.addImage({
            path: screenshotPaths[i],
            x: 0,
            y: 0,
            w: '100%',
            h: '100%'
        });
    }

    // Save PowerPoint
    const outputPath = path.join(__dirname, 'TELKOMSEL_PRESENTATION_FROM_HTML.pptx');
    await pptx.writeFile({ fileName: outputPath });

    // Clean up temp files
    console.log('🧹 Cleaning up...');
    for (const screenshotPath of screenshotPaths) {
        fs.unlinkSync(screenshotPath);
    }

    console.log(`✅ PowerPoint created: ${outputPath}`);
    console.log('📦 File size:', (fs.statSync(outputPath).size / 1024 / 1024).toFixed(2), 'MB');
}

// Run conversion
convertHTMLtoPPTX().catch(err => {
    console.error('❌ Error:', err);
    process.exit(1);
});
