import {Injectable} from '@angular/core';
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';

@Injectable({
  providedIn: 'root'
})
export class ImageHandler {
  constructor(
    private sanitizer: DomSanitizer) {
  }

  prepareUserPicture(imgFile: any): Promise<number[]> {
    return new Promise((resolve, reject) => {
      let reader = new FileReader();

      reader.onloadend = () => {
        const base64String = reader.result as string;

        // Create an image element
        let img = new Image();
        img.onload = () => {
          // Create a canvas element
          let canvas = document.createElement('canvas');
          let ctx = canvas.getContext('2d');

          // Set canvas size
          const size = 100;
          canvas.width = size;
          canvas.height = size;

          // Draw the image onto the canvas, resizing it
          ctx.drawImage(img, 0, 0, size, size);

          // Create a circular vignette
          ctx.globalCompositeOperation = 'destination-in';
          ctx.beginPath();
          ctx.arc(size / 2, size / 2, size / 2, 0, Math.PI * 2, true);
          ctx.closePath();
          ctx.fill();

          // Convert the canvas content to a base64 string
          const base64CroppedImg = canvas.toDataURL('image/png');

          // Convert the base64 string to a Uint8Array
          const arrayBuffer = this.base64ToArrayBuffer(base64CroppedImg);
          const imageBytes = Array.from(new Uint8Array(arrayBuffer));

          resolve(imageBytes);
        };

        img.onerror = () => {
          reject(new Error('Error in loading image'));
        };

        img.src = base64String;
      };

      reader.onerror = () => {
        reject(new Error('Error in reading file'));
      };

      reader.readAsDataURL(imgFile);
    });
  }

  prepareRecipePicture (imgFile: any) {
    return new Promise((resolve, reject) => {
      let reader = new FileReader();

      reader.onloadend = () => {
        const base64String = reader.result as string;

        // Create an image element
        let img = new Image();
        img.onload = () => {
          // Convert the base64 string to a Uint8Array
          const arrayBuffer = this.base64ToArrayBuffer(base64String);
          const imageBytes = Array.from(new Uint8Array(arrayBuffer));

          resolve(imageBytes);
        };

        img.onerror = () => {
          reject(new Error('Error in loading image'));
        };

        img.src = base64String;
      };

      reader.onerror = () => {
        reject(new Error('Error in reading file'));
      };

      reader.readAsDataURL(imgFile);
    });
  }

  private base64ToArrayBuffer(base64: string): ArrayBuffer {
    const binaryString = atob(base64.split(',')[1]);
    const length = binaryString.length;
    const bytes = new Uint8Array(length);

    for (let i = 0; i < length; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }

    return bytes.buffer;
  }


  sanitizeUserImage(imageBytes: any): SafeUrl {
    try {
      if (!imageBytes || imageBytes.length === 0) {
        return '/assets/icons/user_default.png'; // Return default Icon
      }
      const dataUrl = `data:image/png;base64,${imageBytes}`;
      return this.sanitizer.bypassSecurityTrustUrl(dataUrl);
    } catch (error) {
      console.error('Error sanitizing image:', error);
      return '/assets/icons/user_default.png'; // Return default Icon
    }
  }

  sanitizeRecipeImage(imageBytes: any): SafeUrl {
    try {
      if (!imageBytes || imageBytes.length === 0) {
        return '/assets/images/recipe_default.png'; // Return default Icon
      }
      const dataUrl = `data:image/jpeg;base64,${imageBytes}`;
      return this.sanitizer.bypassSecurityTrustUrl(dataUrl);
    } catch (error) {
      console.error('Error sanitizing image:', error);
      return '/assets/images/recipe_default.png'; // Return default Icon
    }
  }
}
