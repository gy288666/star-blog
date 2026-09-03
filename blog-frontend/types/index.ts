// 与 docs/api-contract.md 对应的类型定义

export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  parentId?: number
  sortOrder?: number
  postCount?: number
}

export interface Tag {
  id: number
  name: string
  slug: string
  postCount?: number
}

export interface Post {
  id: number
  type: number // 0文章 1页面 2说说
  title: string
  slug: string
  summary?: string
  cover?: string
  status: number // 0草稿 1发布 2密码保护
  password?: string
  authorId?: number
  authorNickname?: string
  views?: number
  upvotes?: number
  isTop?: number
  allowComment?: number
  commentCount?: number
  publishedAt?: string
  createTime?: string
  updateTime?: string
  contentMd?: string
  contentHtml?: string
  categories?: Category[]
  tags?: Tag[]
  guestName?: string
  mine?: boolean
}

export interface PostDetail extends Post {
  prev?: { id: number; title: string; slug: string } | null
  next?: { id: number; title: string; slug: string } | null
}

export interface Comment {
  id: number
  postId: number
  parentId: number
  rootId: number
  author: string
  email?: string
  website?: string
  avatar?: string
  contentMd?: string
  contentHtml?: string
  isAdmin?: number
  createTime?: string
  status?: number
  ip?: string
  userAgent?: string
  postTitle?: string
  children?: Comment[]
}

export interface Shuoshuo {
  id: number
  title?: string
  contentMd?: string
  contentHtml?: string
  cover?: string
  status?: number
  views?: number
  upvotes?: number
  commentCount?: number
  publishedAt?: string
  createTime?: string
}

export interface Friend {
  id: number
  name: string
  url: string
  avatar?: string
  description?: string
  sortOrder?: number
  status?: number
  createTime?: string
}

export interface Banner {
  id: number
  title?: string
  subtitle?: string
  imageUrl?: string
  bgColor?: string
  typingEffect?: number
  isActive?: number
}

export interface PublicSettings {
  siteTitle?: string
  siteSubtitle?: string
  siteLogo?: string
  footerText?: string
  icpText?: string
  bannerTitle?: string
  bannerSubtitle?: string
  bannerImage?: string
  bannerTypingEffect?: string | number
  allowComment?: string | number
  darkModeDefault?: string
  pageBackgroundImage?: string
  pageBackgroundImageDark?: string
  pageBackgroundOpacity?: string
}

export interface ArchiveYear {
  year: string
  posts: { id: number; title: string; slug: string; createTime: string }[]
}

export interface AdminUser {
  id: number
  username: string
  nickname?: string
  avatar?: string
  role?: string
}
