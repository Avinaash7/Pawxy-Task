from yt_dlp import YoutubeDL

def main(link):
  with YoutubeDL() as ydl:
    list = []
    info_dict = ydl.extract_info(link, download=False)
    video_title = info_dict.get('title', None)
    thumbnail_link = info_dict.get("thumbnail",None)
    view_count = info_dict.get("view_count",None)
    likes = info_dict.get("like_count",None)
    filesize = info_dict.get("filesize_approx",None)
    duration = info_dict.get("duration_string",None)
    list.append(likes)
    list.append(view_count)
    list.append(thumbnail_link)
    list.append(video_title)
    list.append(filesize)
    list.append(duration)

  return list
