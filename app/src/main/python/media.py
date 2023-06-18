import os
import yt_dlp
from com.chaquo.python import Python


download_listener = None



class DownloadProgressCallback:

    def send(self, progress):
        if download_listener is not None:
            percent = progress['_percent_str']
            total_bytes = progress['_total_bytes_str']
            download_listener.updateProgress(percent, total_bytes)





def download_media(url,progress_listener):
    global download_listener
    download_listener = progress_listener

    progress_callback = DownloadProgressCallback()
    files_dir = "/storage/emulated/0/Download/"

    ydl_opts = {
        'format': 'best',
        'outtmpl': os.path.join(files_dir, '%(title)s.%(ext)s'),
        'progress_hooks': [progress_callback.send],
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info_dict = ydl.extract_info(url, download=False)
        title = info_dict.get('title', None)
        title = title.strip()  # Trim trailing spaces

        ydl.download([url])

    download_listener = None

    return title


def get_Size(url):
    ydl_opts = {
        'format': 'best',
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(url, download=False)
        return info['filesize_approx']