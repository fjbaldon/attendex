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
        const dataUrl = await htmlToImage.toPng(element, {
            quality: 1.0,
            backgroundColor: '#ffffff',
            canvasWidth: element.scrollWidth * scale,
            canvasHeight: element.scrollHeight * scale,
            pixelRatio: 1,
        });

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
            finalWidth = pagePrintableWidth;
            finalHeight = finalWidth / imgAspectRatio;
        } else {
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
