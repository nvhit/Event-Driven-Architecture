const puppeteer = require('puppeteer');
const path = require('path');

async function convertHtmlToPng(inputFile) {
  const absolutePath = path.resolve(inputFile);
  const outputFile = absolutePath.replace(/\.html$/, '.png');

  const browser = await puppeteer.launch({ headless: true });
  const page = await browser.newPage();

  // Set viewport for high-quality render
  await page.setViewport({ width: 1200, height: 800, deviceScaleFactor: 2 });

  // Load the HTML file
  await page.goto(`file:///${absolutePath.replace(/\\/g, '/')}`, {
    waitUntil: 'networkidle0'
  });

  // Get the SVG bounding box
  const svgBox = await page.evaluate(() => {
    const svg = document.querySelector('svg');
    if (!svg) return null;
    const rect = svg.getBoundingClientRect();
    return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };
  });

  if (!svgBox) {
    console.error('No SVG element found in the file.');
    await browser.close();
    process.exit(1);
  }

  // Screenshot just the SVG element
  const svgElement = await page.$('svg');
  await svgElement.screenshot({
    path: outputFile,
    type: 'png',
    omitBackground: true
  });

  console.log(`Converted: ${outputFile}`);
  await browser.close();
}

const inputFile = process.argv[2] || '3-svg.html';
convertHtmlToPng(inputFile);
