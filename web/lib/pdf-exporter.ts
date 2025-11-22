import jsPDF from 'jspdf';
import * as htmlToImage from 'html-to-image';

export const exportToPdf = async (
    element: HTMLElement,
    fileName: string,
    scale: number = 2
): Promise<void> => {
    if (!element) {
        console.error("PDF export failed: The provided element is null or undefined.");
        return;
    }

    try {
        // 1. Wait for fonts to be fully loaded to avoid text rendering issues
        await document.fonts.ready;

        // 2. Clone the node to manipulate styles safely
        // We append it to the body but position it absolute/off-screen so it renders full width/height
        const clone = element.cloneNode(true) as HTMLElement;

        // Force styling on clone to ensure full rendering
        clone.style.position = 'absolute';
        clone.style.top = '-9999px';
        clone.style.left = '-9999px';
        clone.style.width = `${element.scrollWidth}px`;
        clone.style.height = 'auto';
        clone.style.overflow = 'visible';
        clone.style.zIndex = '-1';
        document.body.appendChild(clone);

        // Wait a tick for DOM to update
        await new Promise(resolve => setTimeout(resolve, 100));

        const width = clone.scrollWidth;
        const height = clone.scrollHeight;

        const dataUrl = await htmlToImage.toPng(clone, {
            quality: 1.0,
            backgroundColor: '#ffffff',
            width: width,
            height: height,
            pixelRatio: scale, // Increase resolution
            style: {
                // Ensure no scrollbars in screenshot
                overflow: 'visible'
            }
        });

        // Clean up clone
        document.body.removeChild(clone);

        const pdf = new jsPDF({
            orientation: 'portrait',
            unit: 'mm',
            format: 'a4',
            compress: true,
        });

        const pdfWidth = pdf.internal.pageSize.getWidth();
        const pdfHeight = pdf.internal.pageSize.getHeight();
        const margin = 15;

        const img = new Image();
        await new Promise<void>((resolve, reject) => {
            img.onload = () => resolve();
            img.onerror = (err) => reject(`Failed to load the captured image data: ${err}`);
            img.src = dataUrl;
        });

        const imgAspectRatio = img.width / img.height;
        const pagePrintableWidth = pdfWidth - margin * 2;
        const pagePrintableHeight = pdfHeight - margin * 2;
        const pageAspectRatio = pagePrintableWidth / pagePrintableHeight;

        let finalWidth: number;
        let finalHeight: number;

        if (imgAspectRatio > pageAspectRatio) {
            // Image is wider than page (fit to width)
            finalWidth = pagePrintableWidth;
            finalHeight = finalWidth / imgAspectRatio;
        } else {
            // Image is taller than page (fit to height)
            finalHeight = pagePrintableHeight;
            finalWidth = finalHeight * imgAspectRatio;
        }

        const x = (pdfWidth - finalWidth) / 2;

        pdf.addImage(dataUrl, 'PNG', x, margin, finalWidth, finalHeight);
        pdf.save(`${fileName}.pdf`);

    } catch (error) {
        console.error("An error occurred during PDF export:", error);
        throw error;
    }
};
